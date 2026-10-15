package com.mohdroid.minesweeper.feature.game.model

data class GameStatsUiModel(
    val mines: Int,
    val foundKeys: Int,
    val totalKeys: Int,
    val remainingLives: Int,
    val startingLives: Int,
    val moves: Int
)
