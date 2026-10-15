package com.mohdroid.minesweeper.feature.game

import com.mohdroid.domain.game.model.board.*
import com.mohdroid.domain.game.model.level.Difficulty
import com.mohdroid.domain.game.model.session.*
import com.mohdroid.domain.game.usecase.interaction.GameInteraction

internal fun gameState(difficulty: Difficulty = Difficulty.EASY) = GameState(
    difficulty = difficulty, player = Position(0, 0),
    moves = 0, totalKeys = 1, foundKeys = 0, mines = 0,
    startingLives = 1, remainingLives = 1, phase = GamePhase.PLAYING,
    board = listOf(
        listOf(CellState(TileKind.START, revealed = true, visited = true), CellState(null), CellState(TileKind.EXIT)),
        listOf(CellState(null), CellState(null), CellState(null))
    )
)

internal class StubInteraction(var result: Result<GameState>) : GameInteraction {
    val moves = mutableListOf<Direction>()
    val reminders = mutableListOf<Pair<Position, Reminder>>()

    override fun movePlayer(direction: Direction): Result<GameState> {
        moves.add(direction)
        return result
    }

    override fun setCellReminder(position: Position, reminder: Reminder): Result<GameState> {
        reminders.add(position to reminder)
        return result
    }
}
