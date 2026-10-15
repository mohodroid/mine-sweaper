package com.mohdroid.minesweeper.feature.level

import com.mohdroid.minesweeper.feature.level.contract.*
import com.mohdroid.minesweeper.feature.level.model.LevelItemUiModel
import com.mohdroid.minesweeper.ui.error.UiError
import org.junit.Assert.*
import org.junit.Test

class LevelReducerTest {
    private fun ready() = LevelUiState(
        loadState = LevelLoadState.READY,
        levels = listOf(LevelItemUiModel("EASY", 0, 3, 4, 2, 1))
    )

    @Test fun `selection creates a typed request without mutating previous state`() {
        val previous = ready()
        val next = LevelReducer.reduce(previous, LevelChange.LevelSelected("EASY"))
        assertNull(previous.pendingNavigation)
        assertEquals(LevelNavigation("EASY"), next.pendingNavigation)
        assertTrue(next.isNavigating)
        assertSame(previous.levels, next.levels)
    }

    @Test fun `unknown choices and duplicate selection do not change state`() {
        val previous = ready()
        assertSame(previous, LevelReducer.reduce(previous, LevelChange.LevelSelected("UNKNOWN")))
        val selected = LevelReducer.reduce(previous, LevelChange.LevelSelected("EASY"))
        assertSame(selected, LevelReducer.reduce(selected, LevelChange.LevelSelected("EASY")))
        assertSame(selected, LevelReducer.reduce(selected, LevelChange.ScreenResumed))
    }

    @Test fun `only matching acknowledgement consumes navigation`() {
        val selected = LevelReducer.reduce(ready(), LevelChange.LevelSelected("EASY"))
        assertSame(selected, LevelReducer.reduce(selected, LevelChange.NavigationHandled(LevelNavigation("HARD"))))
        val handled = LevelReducer.reduce(selected, LevelChange.NavigationHandled(LevelNavigation("EASY")))
        assertNull(handled.pendingNavigation)
        assertTrue(handled.isNavigating)
        val returned = LevelReducer.reduce(handled, LevelChange.ScreenResumed)
        assertFalse(returned.isNavigating)
    }

    @Test fun `retry loading clears stale selection and errors`() {
        val failed = LevelReducer.reduce(ready(), LevelChange.LoadingFailed(UiError.LEVEL_LOADING_FAILED))
        assertEquals(LevelLoadState.ERROR, failed.loadState)
        assertEquals(UiError.LEVEL_LOADING_FAILED, failed.error)
        assertEquals(LevelUiState(), LevelReducer.reduce(failed, LevelChange.LoadingStarted))
    }
}
