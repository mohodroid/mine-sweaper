package com.mohdroid.minesweeper.feature.game.mapper

import com.mohdroid.domain.game.model.level.Difficulty
import com.mohdroid.domain.game.model.board.Direction
import com.mohdroid.domain.game.model.board.Reminder
import com.mohdroid.domain.game.model.board.Position
import com.mohdroid.minesweeper.feature.game.model.CellPositionUiModel
import com.mohdroid.minesweeper.feature.game.model.DifficultyUiModel
import com.mohdroid.minesweeper.feature.game.model.MoveDirectionUi
import com.mohdroid.minesweeper.feature.game.model.ReminderUiModel

internal fun DifficultyUiModel.toDomain(): Difficulty = when (this) {
    DifficultyUiModel.EASY -> Difficulty.EASY
    DifficultyUiModel.MEDIUM -> Difficulty.MEDIUM
    DifficultyUiModel.HARD -> Difficulty.HARD
}

internal fun MoveDirectionUi.toDomain(): Direction = when (this) {
    MoveDirectionUi.UP -> Direction.NORTH
    MoveDirectionUi.DOWN -> Direction.SOUTH
    MoveDirectionUi.LEFT -> Direction.WEST
    MoveDirectionUi.RIGHT -> Direction.EAST
}

internal fun ReminderUiModel.toDomain(): Reminder = when (this) {
    ReminderUiModel.NONE -> Reminder.NONE
    ReminderUiModel.MINE -> Reminder.MINE
    ReminderUiModel.KEY -> Reminder.KEY
}

internal fun CellPositionUiModel.toDomain(): Position = Position(row, column)
