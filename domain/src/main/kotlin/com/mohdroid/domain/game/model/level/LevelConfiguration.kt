package com.mohdroid.domain.game.model.level

/** A requested level, not a generated board or a serialized asset format. */
data class LevelConfiguration(
    val rows: Int,
    val columns: Int,
    val assets: List<LevelAsset>
)
