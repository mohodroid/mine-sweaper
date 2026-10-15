package com.mohdroid.domain.game.model.session

import com.mohdroid.domain.game.model.level.Difficulty
import com.mohdroid.domain.game.model.board.CellState
import com.mohdroid.domain.game.model.board.Position

/** Application contract, independent of engine and presentation types. */
data class GameState(
    val difficulty: Difficulty,
    val player: Position,
    val moves: Int,
    val totalKeys: Int,
    val foundKeys: Int,
    val mines: Int,
    val startingLives: Int,
    val remainingLives: Int,
    val phase: GamePhase,
    val board: List<List<CellState>>
) {
    val exitUnlocked: Boolean get() = foundKeys == totalKeys
}
