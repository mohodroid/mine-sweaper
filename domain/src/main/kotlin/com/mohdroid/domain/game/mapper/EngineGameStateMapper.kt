package com.mohdroid.domain.game.mapper

import com.mohdroid.domain.game.model.level.Difficulty
import com.mohdroid.domain.game.model.board.CellState
import com.mohdroid.domain.game.model.board.Position
import com.mohdroid.domain.game.model.board.TileKind
import com.mohdroid.domain.game.model.board.Reminder
import com.mohdroid.domain.game.model.session.GameState
import com.mohdroid.domain.game.model.session.GamePhase
import com.mohdroid.game.domain.model.CellSnapshot
import com.mohdroid.game.domain.model.FlagKind
import com.mohdroid.game.domain.model.GameSnapshot
import com.mohdroid.game.domain.model.GamePhase as EnginePhase
import com.mohdroid.game.domain.model.TileKind as EngineTile

/** One mapper per session. Never copies unchanged rows/cells or exposes engine types. */
internal class EngineGameStateMapper(private val difficulty: Difficulty) {
    private var previous: GameSnapshot? = null
    private var cached: GameState? = null

    fun map(snapshot: GameSnapshot): GameState {
        val old = previous
        val state = cached
        if (snapshot === old && state != null) return state
        val board = if (snapshot.board === old?.board && state != null) state.board else {
            snapshot.board.mapIndexed { rowIndex, row ->
                val oldRow = old?.board?.getOrNull(rowIndex)
                val mappedRow = state?.board?.getOrNull(rowIndex)
                if (row === oldRow && mappedRow != null) mappedRow else {
                    row.mapIndexed { columnIndex, cell ->
                        if (cell === oldRow?.getOrNull(columnIndex) && mappedRow != null) {
                            mappedRow[columnIndex]
                        } else cell.toDomain()
                    }
                }
            }
        }
        return GameState(
            difficulty, Position(snapshot.player.row, snapshot.player.column),
            snapshot.moves, snapshot.totalKeys, snapshot.foundKeys, snapshot.mines,
            snapshot.startingLives, snapshot.remainingLives,
            when (snapshot.phase) {
                EnginePhase.PLAYING -> GamePhase.PLAYING
                EnginePhase.WON -> GamePhase.WON
                EnginePhase.LOST -> GamePhase.LOST
            }, board
        ).also {
            previous = snapshot
            cached = it
        }
    }

    private fun CellSnapshot.toDomain() = CellState(
        tile = when (tile) {
            null -> null
            EngineTile.START -> TileKind.START
            EngineTile.EXIT -> TileKind.EXIT
            EngineTile.BLOCK -> TileKind.BLOCK
            EngineTile.MINE -> TileKind.MINE
            EngineTile.KEY -> TileKind.KEY
            EngineTile.STEP -> TileKind.STEP
        },
        revealed = revealed,
        visited = visited,
        reminder = when (flag) {
            FlagKind.NONE -> Reminder.NONE
            FlagKind.MINE -> Reminder.MINE
            FlagKind.KEY -> Reminder.KEY
        },
        adjacentMines = adjacentMines,
        adjacentKeys = adjacentKeys
    )
}
