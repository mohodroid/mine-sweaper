package com.mohdroid.game.domain.model

/** Player-visible cell state. A null tile keeps its contents secret. */
data class CellSnapshot(
    val tile: TileKind?,
    val revealed: Boolean = false,
    val visited: Boolean = false,
    val flag: FlagKind = FlagKind.NONE,
    val adjacentMines: Int? = null,
    val adjacentKeys: Int? = null
)
