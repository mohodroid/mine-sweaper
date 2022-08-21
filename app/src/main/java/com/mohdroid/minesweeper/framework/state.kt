package com.mohdroid.minesweeper.framework

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState

@Composable
fun <T : Any> DiscountopiaStatefulViewModel<T>.state(): State<T> {
    return uiState().collectAsState(initial = currentState)
}