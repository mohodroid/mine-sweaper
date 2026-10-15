package com.mohdroid.minesweeper.ui.error

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mohdroid.minesweeper.R

@Composable
internal fun UiError.message(): String = stringResource(
    when (this) {
        UiError.LEVEL_LOADING_FAILED -> R.string.unable_to_load_levels
        UiError.GAME_LOADING_FAILED -> R.string.unable_to_load_game
        UiError.INVALID_LEVEL_SELECTION -> R.string.invalid_level_selection
    }
)
