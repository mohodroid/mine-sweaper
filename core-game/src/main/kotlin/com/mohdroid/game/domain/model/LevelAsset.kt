package com.mohdroid.game.domain.model

data class LevelAsset(
    val tile: TileKind,
    val count: Int,
    val positions: List<Position> = emptyList()
)
