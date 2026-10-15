package com.mohdroid.minesweeper.feature.game.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohdroid.minesweeper.feature.game.model.CellPositionUiModel
import com.mohdroid.minesweeper.feature.game.model.GameBoardUiModel

@Composable
internal fun GameBoard(
    game: GameBoardUiModel,
    selected: CellPositionUiModel?,
    onCellSelected: (CellPositionUiModel) -> Unit,
    modifier: Modifier
) {
    BoxWithConstraints(modifier, contentAlignment = Alignment.TopCenter) {
        val cellWidth = maxWidth / game.columns
        val cellHeight = maxHeight / game.rows
        val cellSize = maxOf(32.dp, minOf(cellWidth, cellHeight))
        Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Column(Modifier.horizontalScroll(rememberScrollState())) {
                game.cells.forEachIndexed { row, cells ->
                    Row {
                        cells.forEachIndexed { column, cell ->
                            val position = CellPositionUiModel(row, column)
                            GameCell(
                                modifier = Modifier.size(cellSize),
                                cell = cell,
                                isPlayer = game.player == position,
                                status = game.status,
                                selected = selected == position,
                                row = row,
                                column = column,
                                onClick = { onCellSelected(position) }
                            )
                        }
                    }
                }
            }
        }
    }
}
