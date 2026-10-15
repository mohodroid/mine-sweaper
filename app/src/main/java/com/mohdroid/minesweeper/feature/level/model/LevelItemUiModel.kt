package com.mohdroid.minesweeper.feature.level.model

/** The identifier crosses navigation; display details remain owned by this feature. */
data class LevelItemUiModel(
    val id: String,
    val titleResource: Int,
    val rows: Int,
    val columns: Int,
    val mines: Int,
    val keys: Int
)
