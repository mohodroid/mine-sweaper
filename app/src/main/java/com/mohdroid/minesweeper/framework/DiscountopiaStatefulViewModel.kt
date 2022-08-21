package com.mohdroid.minesweeper.framework

open class DiscountopiaStatefulViewModel<STATE : Any>(
    initialState: STATE,
    dispatcherProvider: CoroutineDispatcherProvider = CoroutineDispatcherProviderImpl()
) : StatefulViewModel<STATE>(initialState, dispatcherProvider, true)