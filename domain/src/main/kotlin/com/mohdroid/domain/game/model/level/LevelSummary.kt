package com.mohdroid.domain.game.model.level

/** Selection metadata, not a generated board or its hidden cell contents. */
data class LevelSummary(
    val difficulty: Difficulty,
    val rows: Int,
    val columns: Int,
    val mines: Int,
    val keys: Int
)
