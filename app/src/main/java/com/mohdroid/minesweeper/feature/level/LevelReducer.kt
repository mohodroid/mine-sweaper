package com.mohdroid.minesweeper.feature.level

import com.mohdroid.minesweeper.feature.level.contract.LevelChange
import com.mohdroid.minesweeper.feature.level.contract.LevelNavigation
import com.mohdroid.minesweeper.feature.level.contract.LevelLoadState
import com.mohdroid.minesweeper.feature.level.contract.LevelUiState

internal object LevelReducer {
    fun reduce(state: LevelUiState, change: LevelChange): LevelUiState = when (change) {
        LevelChange.LoadingStarted -> LevelUiState()
        is LevelChange.LevelsLoaded -> LevelUiState(
            loadState = LevelLoadState.READY, levels = change.levels
        )
        is LevelChange.LoadingFailed -> LevelUiState(
            loadState = LevelLoadState.ERROR, error = change.error
        )
        is LevelChange.LevelSelected -> if (
            state.loadState == LevelLoadState.READY && !state.isNavigating &&
            state.levels.any { it.id == change.levelId }
        ) state.copy(
            selectedLevelId = change.levelId, pendingNavigation = LevelNavigation(change.levelId), isNavigating = true
        ) else state
        is LevelChange.NavigationHandled -> if (state.pendingNavigation == change.navigation) {
            // Keep choices disabled until the level destination resumes after gameplay.
            state.copy(pendingNavigation = null)
        } else state
        LevelChange.ScreenResumed -> if (state.pendingNavigation == null && state.isNavigating) {
            state.copy(isNavigating = false)
        } else state
    }
}
