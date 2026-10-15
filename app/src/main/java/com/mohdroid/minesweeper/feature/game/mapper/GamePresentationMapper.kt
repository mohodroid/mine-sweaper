package com.mohdroid.minesweeper.feature.game.mapper

import com.mohdroid.domain.game.model.board.CellState
import com.mohdroid.domain.game.model.board.Reminder
import com.mohdroid.domain.game.model.session.GamePhase
import com.mohdroid.domain.game.model.session.GameState
import com.mohdroid.domain.game.model.board.TileKind
import com.mohdroid.minesweeper.feature.game.model.CellContentUiModel
import com.mohdroid.minesweeper.feature.game.model.CellPositionUiModel
import com.mohdroid.minesweeper.feature.game.model.CellUiModel
import com.mohdroid.minesweeper.feature.game.model.GameBoardUiModel
import com.mohdroid.minesweeper.feature.game.model.GameStatsUiModel
import com.mohdroid.minesweeper.feature.game.model.GameStatusUiModel
import com.mohdroid.minesweeper.feature.game.model.ReminderUiModel

/**
 * Adapts the application's domain contract into visual states.
 * Owned by one ViewModel; unchanged rows/cells retain identity across actions.
 */
internal class GamePresentationMapper {
    private var previousSnapshot: GameState? = null
    private var previousBoard: GameBoardUiModel? = null

    fun reset() {
        previousSnapshot = null
        previousBoard = null
    }

    fun map(snapshot: GameState): GameBoardUiModel {
        val previous = previousSnapshot
        val cached = previousBoard
        if (snapshot === previous && cached != null) return cached

        // These global values affect exit appearance and reminder availability.
        val sameContext = previous != null && cached != null &&
            snapshot.phase == previous.phase && snapshot.exitUnlocked == previous.exitUnlocked
        val cells = if (sameContext && snapshot.board === previous?.board) {
            checkNotNull(cached).cells
        } else {
            snapshot.board.mapIndexed { rowIndex, row ->
                val previousRow = previous?.board?.getOrNull(rowIndex)
                val cachedRow = cached?.cells?.getOrNull(rowIndex)
                if (sameContext && row === previousRow && cachedRow != null) {
                    cachedRow
                } else {
                    row.mapIndexed { columnIndex, cell ->
                        if (sameContext && cell === previousRow?.getOrNull(columnIndex) && cachedRow != null) {
                            cachedRow[columnIndex]
                        } else {
                            cell.toUiModel(snapshot)
                        }
                    }
                }
            }
        }
        return GameBoardUiModel(
            cells = cells,
            player = CellPositionUiModel(snapshot.player.row, snapshot.player.column),
            stats = GameStatsUiModel(
                mines = snapshot.mines,
                foundKeys = snapshot.foundKeys,
                totalKeys = snapshot.totalKeys,
                remainingLives = snapshot.remainingLives,
                startingLives = snapshot.startingLives,
                moves = snapshot.moves
            ),
            status = when (snapshot.phase) {
                GamePhase.PLAYING -> GameStatusUiModel.IN_PROGRESS
                GamePhase.WON -> GameStatusUiModel.WON
                GamePhase.LOST -> GameStatusUiModel.LOST
            }
        ).also {
            previousSnapshot = snapshot
            previousBoard = it
        }
    }

    private fun CellState.toUiModel(game: GameState): CellUiModel = CellUiModel(
        content = when (tile) {
            null -> CellContentUiModel.COVERED
            TileKind.START -> if (visited) CellContentUiModel.VISITED else CellContentUiModel.START
            TileKind.STEP -> if (visited) CellContentUiModel.VISITED else CellContentUiModel.COVERED
            TileKind.BLOCK -> CellContentUiModel.BLOCK
            TileKind.MINE -> CellContentUiModel.MINE
            TileKind.KEY -> if (visited) CellContentUiModel.COLLECTED_KEY else CellContentUiModel.KEY
            TileKind.EXIT -> if (game.exitUnlocked) CellContentUiModel.OPEN_EXIT else CellContentUiModel.LOCKED_EXIT
        },
        reminder = when (reminder) {
            Reminder.NONE -> ReminderUiModel.NONE
            Reminder.MINE -> ReminderUiModel.MINE
            Reminder.KEY -> ReminderUiModel.KEY
        },
        mineClue = adjacentMines,
        keyClue = adjacentKeys,
        canSetReminder = tile == null && game.phase == GamePhase.PLAYING,
        accessibilityContent = tile?.name?.lowercase() ?: "covered cell"
    )
}
