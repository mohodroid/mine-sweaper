package com.mohdroid.minesweeper.feature.level

import com.mohdroid.domain.game.model.level.Difficulty
import com.mohdroid.domain.game.model.level.LevelSummary
import com.mohdroid.domain.game.usecase.level.LoadLevelsUseCase
import com.mohdroid.minesweeper.feature.TestViewModelOwner
import com.mohdroid.minesweeper.feature.level.contract.*
import com.mohdroid.minesweeper.ui.error.UiError
import kotlinx.coroutines.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Test

class LevelViewModelTest {
    private val owner = TestViewModelOwner()
    @After fun clearViewModels() = owner.clear()

    @Test fun `catalogue loads once and maps all display metadata`() {
        var loads = 0
        val vm = viewModel { loads++; Result.success(levels()) }
        vm.onIntent(LevelIntent.Load)
        assertEquals(1, loads)
        assertEquals(LevelLoadState.READY, vm.uiState.value.loadState)
        val level = vm.uiState.value.levels.first()
        assertEquals("EASY", level.id)
        assertEquals(3, level.rows)
        assertEquals(4, level.columns)
        assertEquals(2, level.mines)
        assertEquals(1, level.keys)
    }

    @Test fun `load errors are typed and retry can recover`() {
        var fail = true
        val vm = viewModel {
            if (fail) Result.failure(IllegalStateException("Internal file path"))
            else Result.success(levels())
        }
        assertEquals(LevelLoadState.ERROR, vm.uiState.value.loadState)
        assertEquals(UiError.LEVEL_LOADING_FAILED, vm.uiState.value.error)
        fail = false
        vm.onIntent(LevelIntent.Retry)
        assertEquals(LevelLoadState.READY, vm.uiState.value.loadState)
        assertNull(vm.uiState.value.error)
    }

    @Test fun `invalid selection does not navigate`() {
        val vm = viewModel()
        val before = vm.uiState.value
        vm.onIntent(LevelIntent.SelectLevel("UNKNOWN"))
        assertSame(before, vm.uiState.value)
    }

    @Test fun `selection retains its request and suppresses rapid repeated taps`() {
        val vm = viewModel()
        vm.onIntent(LevelIntent.SelectLevel("EASY"))
        val selected = vm.uiState.value
        vm.onIntent(LevelIntent.SelectLevel("HARD"))
        vm.onIntent(LevelIntent.ScreenResumed)
        assertSame(selected, vm.uiState.value)
        assertEquals(LevelNavigation("EASY"), selected.pendingNavigation)
        assertTrue(selected.isNavigating)
        vm.onIntent(LevelIntent.NavigationHandled(LevelNavigation("HARD")))
        assertSame(selected, vm.uiState.value)
    }

    @Test fun `acknowledgement does not unlock choices until returning from gameplay`() {
        val vm = viewModel()
        vm.onIntent(LevelIntent.SelectLevel("EASY"))
        vm.onIntent(LevelIntent.NavigationHandled(LevelNavigation("EASY")))
        assertNull(vm.uiState.value.pendingNavigation)
        assertTrue(vm.uiState.value.isNavigating)
        vm.onIntent(LevelIntent.SelectLevel("HARD"))
        assertEquals("EASY", vm.uiState.value.selectedLevelId)
        vm.onIntent(LevelIntent.ScreenResumed)
        assertFalse(vm.uiState.value.isNavigating)
        vm.onIntent(LevelIntent.SelectLevel("HARD"))
        assertEquals(LevelNavigation("HARD"), vm.uiState.value.pendingNavigation)
    }

    @Test fun `selection during loading is ignored and loading is not restarted`() = runBlocking {
        val release = CompletableDeferred<Unit>()
        var loads = 0
        val vm = viewModel {
            loads++
            release.await()
            Result.success(levels())
        }
        vm.onIntent(LevelIntent.SelectLevel("EASY"))
        vm.onIntent(LevelIntent.Load)
        vm.onIntent(LevelIntent.Retry)
        assertEquals(LevelLoadState.LOADING, vm.uiState.value.loadState)
        assertNull(vm.uiState.value.pendingNavigation)
        assertEquals(1, loads)
        release.complete(Unit)
        yield()
        assertEquals(LevelLoadState.READY, vm.uiState.value.loadState)
    }

    @Test fun `clearing a loading ViewModel prevents a late catalogue result`() = runBlocking {
        val release = CompletableDeferred<Unit>()
        val vm = viewModel {
            withContext(NonCancellable) { release.await() }
            Result.success(levels())
        }
        val loading = vm.uiState.value
        owner.clear()
        release.complete(Unit)
        yield()
        assertSame(loading, vm.uiState.value)
    }

    @Test fun `empty catalogue renders ready without selectable levels`() {
        val vm = viewModel { Result.success(emptyList()) }
        assertEquals(LevelLoadState.READY, vm.uiState.value.loadState)
        assertTrue(vm.uiState.value.levels.isEmpty())
    }

    private fun levels() = Difficulty.values().map { LevelSummary(it, 3, 4, 2, 1) }

    private fun viewModel(
        load: suspend () -> Result<List<LevelSummary>> = { Result.success(levels()) }
    ) = owner.keep(LevelViewModel(
        loadLevels = object : LoadLevelsUseCase {
            override suspend fun invoke() = load()
        },
        uiDispatcher = Dispatchers.Unconfined
    ))
}
