package com.mohdroid.minesweeper.feature.game.contract

import com.mohdroid.minesweeper.feature.game.model.CellPositionUiModel
import com.mohdroid.minesweeper.feature.game.model.DifficultyUiModel
import com.mohdroid.minesweeper.feature.game.model.GameBoardUiModel

import com.mohdroid.minesweeper.ui.error.UiError

internal sealed interface GameChange {
    data class LoadingStarted(val difficulty: DifficultyUiModel) : GameChange
    data class GameLoaded(val board: GameBoardUiModel) : GameChange
    data class GameUpdated(val board: GameBoardUiModel) : GameChange
    data class LoadingFailed(val error: UiError) : GameChange
    data class CellSelected(val position: CellPositionUiModel) : GameChange
    object ReviewRequested : GameChange
    object Exited : GameChange
    data class NavigationHandled(val navigation: GameNavigation) : GameChange
}
