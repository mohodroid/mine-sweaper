package com.mohdroid.minesweeper.feature.level.mapper

import com.mohdroid.domain.game.model.level.Difficulty
import com.mohdroid.domain.game.model.level.LevelSummary
import com.mohdroid.minesweeper.R
import com.mohdroid.minesweeper.feature.level.model.LevelItemUiModel

internal fun LevelSummary.toUiModel() = LevelItemUiModel(
    id = difficulty.name,
    titleResource = when (difficulty) {
        Difficulty.EASY -> R.string.easy
        Difficulty.MEDIUM -> R.string.medium
        Difficulty.HARD -> R.string.hard
    },
    rows = rows, columns = columns, mines = mines, keys = keys
)
