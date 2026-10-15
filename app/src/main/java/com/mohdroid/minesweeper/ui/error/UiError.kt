package com.mohdroid.minesweeper.ui.error

/** Presentation categories, not exception messages or domain failures. */
enum class UiError(val canRetry: Boolean) {
    LEVEL_LOADING_FAILED(true),
    GAME_LOADING_FAILED(true),
    INVALID_LEVEL_SELECTION(false)
}
