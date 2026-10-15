package com.mohdroid.minesweeper.ui.lifecycle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/** Lifecycle-aware collection compatible with this project's existing Lifecycle/Compose versions. */
@Composable
internal fun <T> StateFlow<T>.collectAsStateWithLifecycle(): State<T> {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    return produceState(initialValue = value, this, lifecycle) {
        collectWhileActive(lifecycle) { value = it }
    }
}

/**
 * Register and collect on the caller's UI dispatcher. Stop subscriptions below minActiveState;
 * resubscribe to the latest StateFlow value on return. Navigation uses RESUMED, rendering STARTED.
 */
internal suspend fun <T> StateFlow<T>.collectWhileActive(
    lifecycle: Lifecycle,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    onActive: () -> Unit = {},
    onValue: (T) -> Unit
): Unit = coroutineScope {
    require(minActiveState == Lifecycle.State.STARTED || minActiveState == Lifecycle.State.RESUMED)
    if (lifecycle.currentState == Lifecycle.State.DESTROYED) return@coroutineScope
    var subscription: Job? = null
    val destroyed = CompletableDeferred<Unit>()

    fun updateSubscription() {
        if (lifecycle.currentState.isAtLeast(minActiveState)) {
            if (subscription == null) {
                // Assign before starting: an initial emission can synchronously navigate and stop us.
                subscription = launch(start = CoroutineStart.LAZY) {
                    onActive()
                    collect { onValue(it) }
                }
                subscription?.start()
            }
        } else {
            subscription?.cancel()
            subscription = null
            if (lifecycle.currentState == Lifecycle.State.DESTROYED) destroyed.complete(Unit)
        }
    }

    val observer = LifecycleEventObserver { _, _ -> updateSubscription() }
    lifecycle.addObserver(observer)
    try {
        updateSubscription()
        destroyed.await()
    } finally {
        lifecycle.removeObserver(observer)
        subscription?.cancel()
    }
}
