package com.mohdroid.minesweeper.feature.game.mapper

import com.mohdroid.domain.game.model.board.*
import com.mohdroid.domain.game.model.level.*
import com.mohdroid.domain.game.model.session.*
import com.mohdroid.minesweeper.feature.game.model.*
import org.junit.Assert.assertEquals
import org.junit.Test

class GameActionMapperTest {
    @Test
    fun `every screen difficulty maps explicitly`() {
        assertEquals(Difficulty.EASY, DifficultyUiModel.EASY.toDomain())
        assertEquals(Difficulty.MEDIUM, DifficultyUiModel.MEDIUM.toDomain())
        assertEquals(Difficulty.HARD, DifficultyUiModel.HARD.toDomain())
    }

    @Test
    fun `screen directions map to the engine coordinate convention`() {
        assertEquals(Direction.NORTH, MoveDirectionUi.UP.toDomain())
        assertEquals(Direction.SOUTH, MoveDirectionUi.DOWN.toDomain())
        assertEquals(Direction.WEST, MoveDirectionUi.LEFT.toDomain())
        assertEquals(Direction.EAST, MoveDirectionUi.RIGHT.toDomain())
    }

    @Test
    fun `reminders and cell coordinates map without changing their meaning`() {
        assertEquals(Reminder.NONE, ReminderUiModel.NONE.toDomain())
        assertEquals(Reminder.MINE, ReminderUiModel.MINE.toDomain())
        assertEquals(Reminder.KEY, ReminderUiModel.KEY.toDomain())
        assertEquals(Position(2, 4), CellPositionUiModel(2, 4).toDomain())
    }
}
