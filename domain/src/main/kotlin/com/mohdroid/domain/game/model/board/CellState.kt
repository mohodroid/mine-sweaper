package com.mohdroid.domain.game.model.board

/** Player-visible information only; null tile means its contents are still hidden. */
data class CellState(
    val tile: TileKind?,
    val revealed: Boolean = false,
    val visited: Boolean = false,
    val reminder: Reminder = Reminder.NONE,
    val adjacentMines: Int? = null,
    val adjacentKeys: Int? = null
)
