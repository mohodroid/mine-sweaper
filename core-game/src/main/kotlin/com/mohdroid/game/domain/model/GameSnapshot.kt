package com.mohdroid.game.domain.model

data class GameSnapshot(
    val player: Position,
    val moves: Int,
    val totalKeys: Int,
    val foundKeys: Int,
    val mines: Int,
    val startingLives: Int,
    val remainingLives: Int,
    val phase: GamePhase,
    val board: List<List<CellSnapshot>>
) {
    val size: BoardSize get() = BoardSize(board.size, board.firstOrNull()?.size ?: 0)
    val exitUnlocked: Boolean get() = foundKeys == totalKeys
}
