package com.mohdroid.minesweeper.framework

import kotlinx.coroutines.CoroutineDispatcher

interface CoroutineDispatcherProvider {
    fun bgDispatcher(): CoroutineDispatcher
    fun ioDispatcher(): CoroutineDispatcher
    fun uiDispatcher(): CoroutineDispatcher
    fun immediateOnUiDispatcher(): CoroutineDispatcher
}