package com.mohdroid.domain.game.model.level

import com.mohdroid.domain.game.model.board.Position
import com.mohdroid.domain.game.model.board.TileKind

data class LevelAsset(
    val tile: TileKind,
    val count: Int,
    val positions: List<Position> = emptyList()
)
