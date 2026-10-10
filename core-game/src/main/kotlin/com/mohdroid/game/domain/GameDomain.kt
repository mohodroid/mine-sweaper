package com.mohdroid.game.domain

import java.util.Collections
import java.util.Random

/** Framework-free game definitions. Android, JSON, DI, and Compose deliberately stay outside this package. */
enum class DifficultyLevel { EASY, MEDIUM, HARD }

enum class TileKind { START, EXIT, BLOCK, MINE, KEY, STEP }

enum class Direction { NORTH, SOUTH, EAST, WEST }

enum class GamePhase { PLAYING, WON, LOST }

enum class FlagKind { NONE, MINE, KEY }

data class BoardSize(val rows: Int, val columns: Int) {
    init {
        require(rows > 0) { "Board rows must be positive." }
        require(columns > 0) { "Board columns must be positive." }
    }

    fun contains(position: Position): Boolean =
        position.row in 0 until rows && position.column in 0 until columns
}

data class Position(val row: Int, val column: Int)

data class LevelAsset(
    val tile: TileKind,
    val count: Int,
    val positions: List<Position> = emptyList()
)

data class LevelDefinition(val size: BoardSize, val assets: List<LevelAsset>) {
    fun validate() {
        require(assets.isNotEmpty()) { "A level must contain assets." }
        require(assets.filter { it.tile == TileKind.START }.sumOf { it.count } == 1) {
            "A level must contain exactly one start tile."
        }
        require(assets.filter { it.tile == TileKind.EXIT }.sumOf { it.count } == 1) {
            "A level must contain exactly one exit tile."
        }

        val explicitPositions = mutableSetOf<Position>()
        var requestedTiles = 0
        assets.forEach { asset ->
            require(asset.count >= 0) { "Asset counts cannot be negative." }
            require(asset.positions.size <= asset.count) {
                "${asset.tile} has more explicit positions than its count."
            }
            asset.positions.forEach { position ->
                require(size.contains(position)) { "Position $position is outside the board." }
                require(explicitPositions.add(position)) { "Duplicate explicit position: $position." }
            }
            requestedTiles += asset.count
        }
        require(requestedTiles <= size.rows * size.columns) {
            "The level requests more tiles than the board can hold."
        }

        val start = assets.single { it.tile == TileKind.START }
        val exit = assets.single { it.tile == TileKind.EXIT }
        require(start.positions.size == 1 && exit.positions.size == 1) {
            "Start and exit positions must be explicitly defined."
        }
    }
}

/** A player's view of a cell. A null tile means its contents are still secret. */
data class Cell(
    val tile: TileKind?,
    val revealed: Boolean = false,
    val visited: Boolean = false,
    val flag: FlagKind = FlagKind.NONE,
    val adjacentMines: Int? = null,
    val adjacentKeys: Int? = null
)

data class GameSnapshot(
    val player: Position,
    val moves: Int,
    val totalKeys: Int,
    val foundKeys: Int,
    val mines: Int,
    val startingLives: Int,
    val remainingLives: Int,
    val phase: GamePhase,
    val board: List<List<Cell>>
) {
    val size: BoardSize get() = BoardSize(board.size, board.firstOrNull()?.size ?: 0)
    val exitUnlocked: Boolean get() = foundKeys == totalKeys
}

/**
 * Stateful implementation with a small public surface: create a session, move, then render snapshots.
 * The mutable board never escapes the domain layer.
 */
class GameSession private constructor(
    private val board: MutableList<MutableList<MutableCell>>,
    private var player: Position,
    private val totalKeys: Int,
    private val mines: Int,
    private val startingLives: Int
) {
    private var foundKeys = 0
    private var moves = 0
    private var remainingLives = startingLives
    private var phase = GamePhase.PLAYING
    private var currentSnapshot = GameSnapshot(
        player = player,
        moves = moves,
        totalKeys = totalKeys,
        foundKeys = foundKeys,
        mines = mines,
        startingLives = startingLives,
        remainingLives = remainingLives,
        phase = phase,
        board = board.indices.map { row -> board[row].indices.map { column -> visibleCell(Position(row, column)) } }
    )

    fun snapshot(): GameSnapshot = currentSnapshot

    fun move(direction: Direction): GameSnapshot {
        if (phase != GamePhase.PLAYING) return currentSnapshot

        val target = player.move(direction)
        if (!contains(target)) return currentSnapshot
        val targetCell = board[target.row][target.column]
        if (targetCell.tile == TileKind.BLOCK || (targetCell.tile == TileKind.MINE && targetCell.revealed)) {
            return currentSnapshot
        }
        if (targetCell.tile == TileKind.EXIT && foundKeys != totalKeys) return currentSnapshot

        val previous = player
        moves++

        if (targetCell.tile == TileKind.MINE) {
            // A hit consumes a life, reveals the mine, and leaves the player on the safe cell.
            targetCell.revealed = true
            targetCell.flag = FlagKind.NONE
            remainingLives--
            if (remainingLives == 0) phase = GamePhase.LOST
        } else {
            val firstVisit = !targetCell.visited
            player = target
            board[previous.row][previous.column].visited = true
            board[previous.row][previous.column].revealed = true
            targetCell.visited = true
            targetCell.revealed = true
            targetCell.flag = FlagKind.NONE
            when (targetCell.tile) {
                TileKind.KEY -> if (firstVisit) foundKeys++
                TileKind.EXIT -> phase = GamePhase.WON
                else -> Unit
            }
        }
        return publish(previous, target)
    }

    /** Flags are editable reminders: they neither block movement nor change the true board. */
    fun setFlag(position: Position, flag: FlagKind): GameSnapshot {
        if (phase != GamePhase.PLAYING || !contains(position)) return currentSnapshot
        val cell = board[position.row][position.column]
        if (cell.revealed || cell.tile == TileKind.BLOCK || cell.tile == TileKind.EXIT || cell.tile == TileKind.START) {
            return currentSnapshot
        }
        val nextFlag = if (flag != FlagKind.NONE && cell.flag == flag) FlagKind.NONE else flag
        if (cell.flag == nextFlag) return currentSnapshot
        cell.flag = nextFlag
        return publish(position)
    }

    private fun publish(vararg changedPositions: Position): GameSnapshot {
        // Revealing the finished board is the one intentional full-board update.
        val revealAll = phase != GamePhase.PLAYING
        val changedRows = mutableMapOf<Int, MutableList<Cell>>()
        fun updateCell(position: Position) {
            val next = visibleCell(position)
            if (currentSnapshot.board[position.row][position.column] == next) return
            val row = changedRows.getOrPut(position.row) {
                currentSnapshot.board[position.row].toMutableList()
            }
            row[position.column] = next
        }
        if (revealAll) {
            board.indices.forEach { row -> board[row].indices.forEach { column -> updateCell(Position(row, column)) } }
        } else {
            changedPositions.forEach(::updateCell)
        }

        // During play, unchanged rows and cells retain identity for Compose to reuse.
        val nextBoard = if (changedRows.isEmpty()) currentSnapshot.board else {
            currentSnapshot.board.toMutableList().also { rows ->
                changedRows.forEach { (rowIndex, row) -> rows[rowIndex] = row }
            }
        }
        currentSnapshot = currentSnapshot.copy(
            player = player,
            moves = moves,
            foundKeys = foundKeys,
            remainingLives = remainingLives,
            phase = phase,
            board = nextBoard
        )
        return currentSnapshot
    }

    private fun visibleCell(position: Position): Cell {
        val source = board[position.row][position.column]
        val visible = source.revealed || phase != GamePhase.PLAYING ||
            source.tile == TileKind.START || source.tile == TileKind.EXIT || source.tile == TileKind.BLOCK
        val hasClue = source.visited && source.tile != TileKind.MINE && source.tile != TileKind.BLOCK
        return Cell(
            tile = if (visible) source.tile else null,
            revealed = visible,
            visited = source.visited,
            flag = source.flag,
            adjacentMines = if (hasClue) adjacentCount(position, TileKind.MINE) else null,
            adjacentKeys = if (hasClue) adjacentCount(position, TileKind.KEY) else null
        )
    }

    private fun adjacentCount(position: Position, tile: TileKind): Int =
        (-1..1).sumOf { rowOffset ->
            (-1..1).count { columnOffset ->
                if (rowOffset == 0 && columnOffset == 0) return@count false
                val neighbor = Position(position.row + rowOffset, position.column + columnOffset)
                contains(neighbor) && board[neighbor.row][neighbor.column].tile == tile
            }
        }

    private fun contains(position: Position): Boolean =
        position.row in board.indices && position.column in board.first().indices

    private data class MutableCell(
        val tile: TileKind,
        var revealed: Boolean = false,
        var visited: Boolean = false,
        var flag: FlagKind = FlagKind.NONE
    )

    companion object {
        fun create(
            level: LevelDefinition,
            seed: Long = System.nanoTime(),
            startingLives: Int = 1
        ): Result<GameSession> = runCatching {
            level.validate()
            require(startingLives > 0) { "A game must start with at least one life." }
            val start = level.assets.single { it.tile == TileKind.START }.positions.single()
            val exit = level.assets.single { it.tile == TileKind.EXIT }.positions.single()
            val board = MutableList(level.size.rows) {
                MutableList(level.size.columns) { MutableCell(TileKind.STEP) }
            }
            val assigned = mutableSetOf<Position>()
            fun place(position: Position, tile: TileKind) {
                require(assigned.add(position)) { "Duplicate tile placement at $position." }
                board[position.row][position.column] = MutableCell(
                    tile = tile,
                    revealed = tile == TileKind.START,
                    visited = tile == TileKind.START
                )
            }

            place(start, TileKind.START)
            place(exit, TileKind.EXIT)
            val protectedPath = directPath(start, exit).toSet()
            val random = Random(seed)

            // Explicit positions are authoritative. Keep the exit corridor free of hazards.
            level.assets.filter { it.tile != TileKind.START && it.tile != TileKind.EXIT }.forEach { asset ->
                asset.positions.forEach { position ->
                    require(asset.tile != TileKind.BLOCK && asset.tile != TileKind.MINE || position !in protectedPath) {
                        "A block or mine cannot occupy the guaranteed start-to-exit route."
                    }
                    place(position, asset.tile)
                }
            }

            placeRandomTiles(
                board = board,
                assigned = assigned,
                protectedPath = protectedPath,
                tile = TileKind.BLOCK,
                amount = level.assets.filter { it.tile == TileKind.BLOCK }.sumOf { it.count - it.positions.size },
                random = random,
                eligible = { position -> position !in protectedPath }
            )

            // Explicit keys need reserved safe routes; generated keys can be chosen after mines.
            val safelyReachable = reachablePositions(board, start, avoidMines = true, avoidExit = true)
            val explicitKeys = level.assets.filter { it.tile == TileKind.KEY }.flatMap { it.positions }
            require(explicitKeys.all { it in safelyReachable }) {
                "Every key must have a mine-free route that does not pass through the locked exit."
            }
            val safeRoutes = protectedPath.toMutableSet()
            explicitKeys.forEach { key ->
                safeRoutes += safePath(board, start, key)
            }
            val connectedBeforeMines = reachablePositions(board, start, avoidMines = false)
            val mineAmount = level.assets.filter { it.tile == TileKind.MINE }.sumOf { it.count - it.positions.size }
            val keyAmount = level.assets.filter { it.tile == TileKind.KEY }.sumOf { it.count - it.positions.size }
            var finishedBoard: MutableList<MutableList<MutableCell>>? = null
            var lastFailure: IllegalArgumentException? = null
            // A mine layout may isolate too many cells for the requested random keys.
            // Retry with the same seeded RNG; fixed assets and protected routes never change.
            for (attempt in 0 until 128) {
                val draft = board.map { row -> row.map { it.copy() }.toMutableList() }.toMutableList()
                val draftAssigned = assigned.toMutableSet()
                try {
                    placeRandomTiles(
                        board = draft,
                        assigned = draftAssigned,
                        protectedPath = safeRoutes,
                        tile = TileKind.MINE,
                        amount = mineAmount,
                        random = random,
                        eligible = { it in connectedBeforeMines }
                    )
                    val finalSafeCells = reachablePositions(draft, start, avoidMines = true, avoidExit = true)
                    require(explicitKeys.all { it in finalSafeCells }) {
                        "Every fixed key must remain safely reachable without crossing the locked exit."
                    }
                    placeRandomTiles(
                        board = draft,
                        assigned = draftAssigned,
                        protectedPath = protectedPath,
                        tile = TileKind.KEY,
                        amount = keyAmount,
                        random = random,
                        eligible = { it in finalSafeCells }
                    )
                    finishedBoard = draft
                    break
                } catch (exception: IllegalArgumentException) {
                    lastFailure = exception
                }
            }
            val playableBoard = requireNotNull(finishedBoard) {
                "Unable to generate a safe board after 128 attempts: ${lastFailure?.message}"
            }

            GameSession(
                board = playableBoard,
                player = start,
                totalKeys = playableBoard.sumOf { row -> row.count { it.tile == TileKind.KEY } },
                mines = playableBoard.sumOf { row -> row.count { it.tile == TileKind.MINE } },
                startingLives = startingLives
            )
        }

        private fun placeRandomTiles(
            board: MutableList<MutableList<MutableCell>>,
            assigned: MutableSet<Position>,
            protectedPath: Set<Position>,
            tile: TileKind,
            amount: Int,
            random: Random,
            eligible: (Position) -> Boolean
        ) {
            val candidates = board.indices.flatMap { row ->
                board[row].indices.map { column -> Position(row, column) }
            }.filter { it !in assigned && it !in protectedPath && eligible(it) }.toMutableList()
            Collections.shuffle(candidates, random)
            require(candidates.size >= amount) { "Not enough valid cells to place $amount $tile tiles." }
            candidates.take(amount).forEach { position ->
                assigned += position
                board[position.row][position.column] = MutableCell(tile)
            }
        }

        private fun directPath(start: Position, end: Position): List<Position> {
            val path = mutableListOf(start)
            var current = start
            while (current.row != end.row) {
                current = current.copy(row = current.row + if (end.row > current.row) 1 else -1)
                path += current
            }
            while (current.column != end.column) {
                current = current.copy(column = current.column + if (end.column > current.column) 1 else -1)
                path += current
            }
            return path
        }

        private fun reachablePositions(
            board: List<List<MutableCell>>,
            start: Position,
            avoidMines: Boolean,
            avoidExit: Boolean = false
        ): Set<Position> {
            val visited = mutableSetOf(start)
            val queue = ArrayDeque<Position>()
            queue.add(start)
            while (queue.isNotEmpty()) {
                val current = queue.removeFirst()
                Direction.values().map { current.move(it) }
                    .filter { candidate ->
                        candidate.row in board.indices &&
                            candidate.column in board.first().indices &&
                            board[candidate.row][candidate.column].tile != TileKind.BLOCK &&
                            (!avoidMines || board[candidate.row][candidate.column].tile != TileKind.MINE) &&
                            (!avoidExit || board[candidate.row][candidate.column].tile != TileKind.EXIT) &&
                            visited.add(candidate)
                    }
                    .forEach(queue::add)
            }
            return visited
        }

        private fun safePath(board: List<List<MutableCell>>, start: Position, destination: Position): Set<Position> {
            val parents = mutableMapOf<Position, Position?>().apply { put(start, null) }
            val queue = ArrayDeque<Position>()
            queue.add(start)
            while (queue.isNotEmpty() && destination !in parents) {
                val current = queue.removeFirst()
                Direction.values().map { current.move(it) }
                    .filter { candidate ->
                        candidate.row in board.indices &&
                            candidate.column in board.first().indices &&
                            board[candidate.row][candidate.column].tile != TileKind.BLOCK &&
                            board[candidate.row][candidate.column].tile != TileKind.MINE &&
                            board[candidate.row][candidate.column].tile != TileKind.EXIT &&
                            candidate !in parents
                    }
                    .forEach { candidate ->
                        parents[candidate] = current
                        queue.add(candidate)
                    }
            }
            require(destination in parents) { "An explicitly positioned key has no mine-free route." }
            val path = mutableSetOf<Position>()
            var current: Position? = destination
            while (current != null) {
                path += current
                current = parents[current]
            }
            return path
        }
    }
}

private fun Position.move(direction: Direction): Position = when (direction) {
    Direction.NORTH -> copy(row = row - 1)
    Direction.SOUTH -> copy(row = row + 1)
    Direction.EAST -> copy(column = column + 1)
    Direction.WEST -> copy(column = column - 1)
}
