package com.mohdroid.minesweeper.framework

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class CoroutineDispatcherProviderImpl : CoroutineDispatcherProvider {
    override fun bgDispatcher(): CoroutineDispatcher = Dispatchers.Default
    override fun ioDispatcher(): CoroutineDispatcher = Dispatchers.IO
    override fun uiDispatcher(): CoroutineDispatcher = Dispatchers.Main
    override fun immediateOnUiDispatcher(): CoroutineDispatcher =Dispatchers.Main.immediate
}