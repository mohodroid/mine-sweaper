package com.mohdroid.minesweeper.ui.lifecycle

import androidx.lifecycle.*
import com.mohdroid.domain.game.model.level.Difficulty
import com.mohdroid.domain.game.model.level.LevelSummary
import com.mohdroid.domain.game.usecase.level.LoadLevelsUseCase
import com.mohdroid.minesweeper.feature.TestViewModelOwner
import com.mohdroid.minesweeper.feature.level.LevelViewModel
import com.mohdroid.minesweeper.feature.level.contract.LevelIntent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.*
import org.junit.Test

class LifecycleCollectionTest {
    @Test fun `state collection stops below started and resumes with latest value`() = runBlocking {
        val lifecycle = FakeLifecycle(Lifecycle.State.CREATED)
        val state = MutableStateFlow(0)
        val seen = mutableListOf<Int>()
        val task = launch(start = CoroutineStart.UNDISPATCHED) {
            state.collectWhileActive(lifecycle) { seen.add(it) }
        }
        yield()
        assertTrue(seen.isEmpty())
        assertEquals(0, state.subscriptionCount.value)
        lifecycle.moveTo(Lifecycle.State.STARTED, Lifecycle.Event.ON_START)
        yield()
        assertEquals(listOf(0), seen)
        state.value = 1
        yield()
        lifecycle.moveTo(Lifecycle.State.CREATED, Lifecycle.Event.ON_STOP)
        yield()
        assertEquals(0, state.subscriptionCount.value)
        state.value = 2
        yield()
        assertEquals(listOf(0, 1), seen)
        lifecycle.moveTo(Lifecycle.State.STARTED, Lifecycle.Event.ON_START)
        yield()
        assertEquals(listOf(0, 1, 2), seen)
        lifecycle.moveTo(Lifecycle.State.DESTROYED, Lifecycle.Event.ON_DESTROY)
        task.join()
        assertEquals(0, lifecycle.observerCount)
    }

    @Test fun `navigation waits for resumed and an acknowledged request is not replayed`() = runBlocking {
        val lifecycle = FakeLifecycle(Lifecycle.State.STARTED)
        val navigation = MutableStateFlow<String?>("EASY")
        val handled = mutableListOf<String>()
        val task = launch(start = CoroutineStart.UNDISPATCHED) {
            navigation.collectWhileActive(lifecycle, Lifecycle.State.RESUMED) { request ->
                if (request != null) {
                    handled.add(request)
                    navigation.value = null
                }
            }
        }
        yield()
        assertTrue(handled.isEmpty())
        lifecycle.moveTo(Lifecycle.State.RESUMED, Lifecycle.Event.ON_RESUME)
        yield()
        assertEquals(listOf("EASY"), handled)
        lifecycle.moveTo(Lifecycle.State.STARTED, Lifecycle.Event.ON_PAUSE)
        lifecycle.moveTo(Lifecycle.State.RESUMED, Lifecycle.Event.ON_RESUME)
        yield()
        assertEquals(listOf("EASY"), handled)
        task.cancelAndJoin()
        assertEquals(0, lifecycle.observerCount)
    }

    @Test fun `synchronous navigation can stop the collector during its first emission`() = runBlocking {
        val lifecycle = FakeLifecycle(Lifecycle.State.RESUMED)
        val state = MutableStateFlow(0)
        var emissions = 0
        val task = launch(start = CoroutineStart.UNDISPATCHED) {
            state.collectWhileActive(lifecycle, Lifecycle.State.RESUMED) {
                emissions++
                lifecycle.moveTo(Lifecycle.State.STARTED, Lifecycle.Event.ON_PAUSE)
            }
        }
        yield()
        state.value = 1
        yield()
        assertEquals(1, emissions)
        assertEquals(0, state.subscriptionCount.value)
        task.cancelAndJoin()
        assertEquals(0, lifecycle.observerCount)
    }

    @Test fun `activation callback runs once per active lifecycle interval`() = runBlocking {
        val lifecycle = FakeLifecycle(Lifecycle.State.RESUMED)
        val state = MutableStateFlow(0)
        var activations = 0
        val task = launch(start = CoroutineStart.UNDISPATCHED) {
            state.collectWhileActive(lifecycle, Lifecycle.State.RESUMED, onActive = { activations++ }) {}
        }
        yield()
        state.value = 1
        yield()
        assertEquals(1, activations)
        lifecycle.moveTo(Lifecycle.State.STARTED, Lifecycle.Event.ON_PAUSE)
        lifecycle.moveTo(Lifecycle.State.RESUMED, Lifecycle.Event.ON_RESUME)
        yield()
        assertEquals(2, activations)
        task.cancelAndJoin()
    }

    @Test fun `destroyed lifecycle is never observed`() = runBlocking {
        val lifecycle = FakeLifecycle(Lifecycle.State.DESTROYED)
        MutableStateFlow(0).collectWhileActive(lifecycle) { fail("Must not collect") }
        assertEquals(0, lifecycle.observerCount)
    }

    @Test fun `level navigation survives backgrounding and unlocks only after returning`() = runBlocking {
        val owner = TestViewModelOwner()
        val vm = owner.keep(LevelViewModel(object : LoadLevelsUseCase {
            override suspend fun invoke() = Result.success(
                Difficulty.values().map { LevelSummary(it, 3, 4, 2, 1) }
            )
        }, Dispatchers.Unconfined))
        val lifecycle = FakeLifecycle(Lifecycle.State.STARTED)
        val navigations = mutableListOf<String>()
        val task = launch(start = CoroutineStart.UNDISPATCHED) {
            vm.uiState.collectWhileActive(
                lifecycle, Lifecycle.State.RESUMED,
                onActive = { vm.onIntent(LevelIntent.ScreenResumed) }
            ) { current ->
                current.pendingNavigation?.let { navigation ->
                    navigations.add(navigation.levelId)
                    // Navigation synchronously pauses the departing destination.
                    lifecycle.moveTo(Lifecycle.State.STARTED, Lifecycle.Event.ON_PAUSE)
                    vm.onIntent(LevelIntent.NavigationHandled(navigation))
                }
            }
        }
        try {
            vm.onIntent(LevelIntent.SelectLevel("EASY"))
            vm.onIntent(LevelIntent.SelectLevel("HARD"))
            yield()
            assertTrue(navigations.isEmpty())
            lifecycle.moveTo(Lifecycle.State.RESUMED, Lifecycle.Event.ON_RESUME)
            yield()
            assertEquals(listOf("EASY"), navigations)
            assertNull(vm.uiState.value.pendingNavigation)
            assertTrue(vm.uiState.value.isNavigating)
            vm.onIntent(LevelIntent.SelectLevel("HARD"))
            assertNull(vm.uiState.value.pendingNavigation)
            lifecycle.moveTo(Lifecycle.State.RESUMED, Lifecycle.Event.ON_RESUME)
            yield()
            assertFalse(vm.uiState.value.isNavigating)
            assertEquals(listOf("EASY"), navigations)
            vm.onIntent(LevelIntent.SelectLevel("HARD"))
            yield()
            assertEquals(listOf("EASY", "HARD"), navigations)
        } finally {
            task.cancelAndJoin()
            owner.clear()
        }
    }

    private class FakeLifecycle(private var state: State) : Lifecycle() {
        private val observers = linkedSetOf<LifecycleObserver>()
        val observerCount: Int get() = observers.size
        override fun getCurrentState() = state
        override fun addObserver(observer: LifecycleObserver) { observers.add(observer) }
        override fun removeObserver(observer: LifecycleObserver) { observers.remove(observer) }

        fun moveTo(next: State, event: Event) {
            state = next
            val source = object : LifecycleOwner { override fun getLifecycle(): Lifecycle = this@FakeLifecycle }
            observers.toList().forEach { (it as LifecycleEventObserver).onStateChanged(source, event) }
        }
    }
}
