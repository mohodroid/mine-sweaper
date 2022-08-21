package com.mohdroid.minesweeper.framework

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

abstract class BaseViewModel(val coroutineDistacherProvider: CoroutineDispatcherProvider) :
    ViewModel(),
    CoroutineScope {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(coroutineDistacherProvider.bgDispatcher() + job)

    override val coroutineContext: CoroutineContext = scope.coroutineContext

    suspend inline fun <T> onBg(crossinline coroutine: suspend () -> T): T =
        withContext(coroutineDistacherProvider.bgDispatcher()) {
            coroutine()
        }

    suspend inline fun <T> onUI(crossinline coroutine: suspend () -> T): T =
        withContext(coroutineDistacherProvider.uiDispatcher()) {
            coroutine()
        }

    suspend inline fun <T> onIO(crossinline coroutine: suspend () -> T): T =
        withContext(coroutineDistacherProvider.ioDispatcher()) {
            coroutine()
        }

    suspend inline fun <T> onUiImmediate(crossinline coroutine: suspend () -> T): T =
        withContext(coroutineDistacherProvider.immediateOnUiDispatcher()) {
            coroutine()
        }

    override fun onCleared() {
        super.onCleared()
        coroutineContext.cancelChildren()
    }
}