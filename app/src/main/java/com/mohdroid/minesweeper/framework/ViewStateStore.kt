package com.mohdroid.minesweeper.framework

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ViewStateStore<T : Any>(
    initialState: T,
    emitInitialState: Boolean = false
) {
    var state = initialState
        set(value) {
            field = value
            _uiState.value = value
        }

    private val _uiState = MutableStateFlow(initialState).apply {
        if (emitInitialState) value = initialState
    }

    val uiState = _uiState as StateFlow<T>

}
