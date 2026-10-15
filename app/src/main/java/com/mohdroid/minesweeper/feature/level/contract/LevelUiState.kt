package com.mohdroid.minesweeper.feature.level.contract

import com.mohdroid.minesweeper.feature.level.model.LevelItemUiModel

import com.mohdroid.minesweeper.ui.error.UiError

enum class LevelLoadState { LOADING, READY, ERROR }

data class LevelUiState(
    val loadState: LevelLoadState = LevelLoadState.LOADING,
    val levels: List<LevelItemUiModel> = emptyList(),
    val selectedLevelId: String? = null,
    val error: UiError? = null,
    val pendingNavigation: LevelNavigation? = null,
    val isNavigating: Boolean = false
)
