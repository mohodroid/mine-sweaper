package com.mohdroid.minesweeper.feature.game.model

data class GameBoardUiModel(
    val cells: List<List<CellUiModel>>,
    val player: CellPositionUiModel,
    val stats: GameStatsUiModel,
    val status: GameStatusUiModel
) {
    val rows: Int get() = cells.size
    val columns: Int get() = cells.firstOrNull()?.size ?: 0
}
