package com.mohdroid.minesweeper.framework

import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PROTECTED
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking

abstract class StatefulViewModel<STATE : Any>(
    initialState: STATE,
    coroutineDispatcherProvider: CoroutineDispatcherProvider,
    emitInitialState: Boolean = false
) : BaseViewModel(coroutineDispatcherProvider) {

    private fun create() = onCreate()

    private val stateStore = ViewStateStore(initialState, emitInitialState)
    private val lazyState: ViewStateStore<STATE> by lazy {
        stateStore.also {
            create()
        }
    }

    protected open fun onCreate() {}

    fun uiState(): StateFlow<STATE> = lazyState.uiState

    val currentState: STATE
        get() {
            return stateStore.state
        }

    /**
     * Is called after the state is successfully updated
     */
    @VisibleForTesting(otherwise = PROTECTED)
    open fun onStateUpdated(currentState: STATE) {}

    /**
     * Used to update states outside of coroutines.
     * This method should only be called on the main thread since it is updating a LiveData.
     * If you are inside a coroutine and a context other than the main dispatcher, use onUI { applyState (...) }
     */
    @VisibleForTesting(otherwise = PROTECTED)
    fun applyState(function: STATE.() -> STATE) = runBlocking {
        return@runBlocking onUiImmediate {
            val oldState = stateStore.state
            val newState = oldState.function()
            if (newState == oldState)
                return@onUiImmediate // don't change the state, since we want distinct changes in the state
            stateStore.state = newState
            onStateUpdated(currentState)
        }
    }
}