package com.mohdroid.minesweeper.feature.game

import com.mohdroid.minesweeper.feature.game.contract.GameChange
import com.mohdroid.minesweeper.feature.game.contract.GameNavigation
import com.mohdroid.minesweeper.feature.game.contract.GameLoadState
import com.mohdroid.minesweeper.feature.game.contract.GameUiState

/** Pure presentation transitions. Move validity, lives, keys and victory belong to the engine. */
internal object GameReducer {
    fun reduce(state: GameUiState, change: GameChange): GameUiState = when (change) {
        is GameChange.LoadingStarted -> GameUiState(
            loadState = GameLoadState.LOADING, difficulty = change.difficulty
        )
        is GameChange.GameLoaded -> state.copy(
            loadState = GameLoadState.READY, game = change.board, error = null
        )
        is GameChange.GameUpdated -> if (change.board === state.game) state else state.copy(
            game = change.board,
            isReviewingBoard = state.isReviewingBoard && state.game?.status == change.board.status
        )
        is GameChange.LoadingFailed -> state.copy(
            loadState = GameLoadState.ERROR, game = null, error = change.error,
            selectedCell = null, isReviewingBoard = false
        )
        is GameChange.CellSelected -> {
            val cell = state.game?.cells?.getOrNull(change.position.row)?.getOrNull(change.position.column)
            if (state.canMove && cell != null) state.copy(selectedCell = change.position) else state
        }
        GameChange.ReviewRequested -> if (state.isFinished) state.copy(isReviewingBoard = true) else state
        GameChange.Exited -> GameUiState(
            pendingNavigation = GameNavigation.BackToLevels, isExiting = true
        )
        is GameChange.NavigationHandled -> if (state.pendingNavigation == change.navigation) {
            state.copy(pendingNavigation = null)
        } else state
    }
}
