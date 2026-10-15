package com.mohdroid.minesweeper.feature.game.contract

import com.mohdroid.minesweeper.feature.game.model.CellPositionUiModel
import com.mohdroid.minesweeper.feature.game.model.DifficultyUiModel
import com.mohdroid.minesweeper.feature.game.model.GameBoardUiModel
import com.mohdroid.minesweeper.feature.game.model.GameStatusUiModel

import com.mohdroid.minesweeper.ui.error.UiError

enum class GameLoadState { IDLE, LOADING, READY, ERROR }

data class GameUiState(
    val loadState: GameLoadState = GameLoadState.IDLE,
    val difficulty: DifficultyUiModel? = null,
    val game: GameBoardUiModel? = null,
    val error: UiError? = null,
    val selectedCell: CellPositionUiModel? = null,
    val isReviewingBoard: Boolean = false,
    val pendingNavigation: GameNavigation? = null,
    val isExiting: Boolean = false
) {
    val canMove: Boolean get() = loadState == GameLoadState.READY &&
        game?.status == GameStatusUiModel.IN_PROGRESS
    val isFinished: Boolean get() = game?.status == GameStatusUiModel.WON ||
        game?.status == GameStatusUiModel.LOST
}
