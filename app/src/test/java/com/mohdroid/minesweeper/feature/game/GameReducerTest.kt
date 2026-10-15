package com.mohdroid.minesweeper.feature.game

import com.mohdroid.minesweeper.feature.game.contract.*
import com.mohdroid.minesweeper.feature.game.mapper.GamePresentationMapper
import com.mohdroid.minesweeper.feature.game.model.*
import com.mohdroid.minesweeper.ui.error.UiError
import org.junit.Assert.*
import org.junit.Test

class GameReducerTest {
    private fun playing() = GameUiState(
        loadState = GameLoadState.READY, difficulty = DifficultyUiModel.EASY,
        game = GamePresentationMapper().map(gameState())
    )

    @Test fun `loading resets presentation selection review error and old board`() {
        val previous = playing().copy(
            selectedCell = CellPositionUiModel(1, 0), isReviewingBoard = true,
            error = UiError.GAME_LOADING_FAILED
        )
        val next = GameReducer.reduce(previous, GameChange.LoadingStarted(DifficultyUiModel.HARD))
        assertEquals(GameUiState(GameLoadState.LOADING, DifficultyUiModel.HARD), next)
    }

    @Test fun `same board update is a no-op and cannot mutate old state`() {
        val state = playing()
        assertSame(state, GameReducer.reduce(state, GameChange.GameUpdated(state.game!!)))
        val selected = GameReducer.reduce(state, GameChange.CellSelected(CellPositionUiModel(1, 0)))
        assertNull(state.selectedCell)
        assertNotNull(selected.selectedCell)
        assertSame(state.game, selected.game)
    }

    @Test fun `out of bounds selection and premature review are ignored`() {
        val state = playing()
        assertSame(state, GameReducer.reduce(state, GameChange.CellSelected(CellPositionUiModel(-1, 0))))
        assertSame(state, GameReducer.reduce(state, GameChange.CellSelected(CellPositionUiModel(99, 0))))
        assertSame(state, GameReducer.reduce(state, GameChange.ReviewRequested))
    }

    @Test fun `terminal review and loading error transitions are explicit`() {
        val state = playing().let { it.copy(game = it.game!!.copy(status = GameStatusUiModel.LOST)) }
        val review = GameReducer.reduce(state, GameChange.ReviewRequested)
        assertTrue(review.isReviewingBoard)
        val failed = GameReducer.reduce(review, GameChange.LoadingFailed(UiError.GAME_LOADING_FAILED))
        assertEquals(GameLoadState.ERROR, failed.loadState)
        assertNull(failed.game)
        assertFalse(failed.isReviewingBoard)
        assertEquals(UiError.GAME_LOADING_FAILED, failed.error)
    }

    @Test fun `exit retains navigation until acknowledgement without restoring gameplay`() {
        val exited = GameReducer.reduce(playing(), GameChange.Exited)
        assertTrue(exited.isExiting)
        assertNull(exited.game)
        assertEquals(GameNavigation.BackToLevels, exited.pendingNavigation)
        val handled = GameReducer.reduce(exited, GameChange.NavigationHandled(GameNavigation.BackToLevels))
        assertNull(handled.pendingNavigation)
        assertTrue(handled.isExiting)
    }
}
