package com.mohdroid.minesweeper.feature.level.contract

import com.mohdroid.minesweeper.feature.level.model.LevelItemUiModel

import com.mohdroid.minesweeper.ui.error.UiError

internal sealed interface LevelChange {
    object LoadingStarted : LevelChange
    data class LevelsLoaded(val levels: List<LevelItemUiModel>) : LevelChange
    data class LoadingFailed(val error: UiError) : LevelChange
    data class LevelSelected(val levelId: String) : LevelChange
    data class NavigationHandled(val navigation: LevelNavigation) : LevelChange
    object ScreenResumed : LevelChange
}
