package com.mohdroid.game.domain.engine

import com.mohdroid.game.domain.model.FlagKind
import com.mohdroid.game.domain.model.TileKind

/** Engine-only mutable storage; never returned through the public snapshot API. */
internal data class Cell(
    val tile: TileKind,
    var revealed: Boolean = false,
    var visited: Boolean = false,
    var flag: FlagKind = FlagKind.NONE
)
