package com.mohdroid.minesweeper.feature.game.model

/** Visual states, not copies of the engine's tile types. */
enum class CellContentUiModel {
    COVERED, START, VISITED, BLOCK, MINE, KEY, COLLECTED_KEY, LOCKED_EXIT, OPEN_EXIT
}
