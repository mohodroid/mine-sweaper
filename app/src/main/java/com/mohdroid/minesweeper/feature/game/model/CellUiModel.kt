package com.mohdroid.minesweeper.feature.game.model

/** Everything needed to render a cell, without exposing domain types. */
data class CellUiModel(
    val content: CellContentUiModel,
    val reminder: ReminderUiModel,
    val mineClue: Int?,
    val keyClue: Int?,
    val canSetReminder: Boolean,
    val accessibilityContent: String
)
