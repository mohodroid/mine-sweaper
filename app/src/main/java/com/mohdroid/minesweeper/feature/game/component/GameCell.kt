package com.mohdroid.minesweeper.feature.game.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohdroid.minesweeper.R
import com.mohdroid.minesweeper.feature.game.model.CellContentUiModel
import com.mohdroid.minesweeper.feature.game.model.CellUiModel
import com.mohdroid.minesweeper.feature.game.model.GameStatusUiModel
import com.mohdroid.minesweeper.feature.game.model.ReminderUiModel

@Composable
private fun cellPainter(
    cell: CellUiModel,
    isPlayer: Boolean,
    status: GameStatusUiModel
): Painter = if (isPlayer) {
    when (status) {
        GameStatusUiModel.WON -> painterResource(R.drawable.ic_cell_happy_s)
        GameStatusUiModel.LOST -> painterResource(R.drawable.ic_cell_sad_s)
        GameStatusUiModel.IN_PROGRESS -> painterResource(R.drawable.ic_cell_soldier)
    }
} else {
    when (cell.content) {
        CellContentUiModel.COVERED -> painterResource(R.drawable.ic_cell__2_)
        CellContentUiModel.START -> painterResource(R.drawable.ic_cell_soldier)
        CellContentUiModel.VISITED -> painterResource(R.drawable.ic_cell_walked)
        CellContentUiModel.BLOCK -> painterResource(R.drawable.ic_cell_block)
        CellContentUiModel.MINE -> painterResource(R.drawable.ic_cell_mine)
        CellContentUiModel.KEY -> painterResource(R.drawable.ic_cell_key)
        CellContentUiModel.COLLECTED_KEY -> painterResource(R.drawable.ic_cell_key_founded)
        CellContentUiModel.LOCKED_EXIT -> painterResource(R.drawable.ic_cell_close_door)
        CellContentUiModel.OPEN_EXIT -> painterResource(R.drawable.ic_cell_open_door)
    }
}

@Composable
fun GameCell(
    modifier: Modifier = Modifier,
    cell: CellUiModel,
    isPlayer: Boolean,
    status: GameStatusUiModel,
    selected: Boolean,
    row: Int,
    column: Int,
    onClick: () -> Unit
) {
    val flagLabel = when (cell.reminder) {
        ReminderUiModel.NONE -> ""
        ReminderUiModel.MINE -> ", suspected mine"
        ReminderUiModel.KEY -> ", suspected key"
    }
    val clueLabel = if (cell.mineClue != null && cell.keyClue != null) {
        ", adjacent mines ${cell.mineClue}, adjacent keys ${cell.keyClue}"
    } else ""
    val label = "Row ${row + 1}, column ${column + 1}, " +
        (if (isPlayer) "player on " else "") +
        cell.accessibilityContent + flagLabel + clueLabel
    Box(
        modifier = modifier
            .padding(0.2.dp)
            .then(if (selected) Modifier.border(2.dp, Color.Yellow) else Modifier)
            .clickable(enabled = cell.canSetReminder, onClick = onClick)
            .semantics { contentDescription = label }
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = cellPainter(cell, isPlayer, status),
            contentScale = ContentScale.FillBounds,
            contentDescription = null
        )
        if (cell.mineClue != null && cell.keyClue != null) {
            Column(
                modifier = Modifier.align(Alignment.BottomEnd).background(Color.White.copy(alpha = 0.82f)),
                horizontalAlignment = Alignment.End
            ) {
                Text("M${cell.mineClue}", fontSize = 10.sp, color = Color.Red, lineHeight = 10.sp, fontWeight = FontWeight.Bold)
                Text("K${cell.keyClue}", fontSize = 10.sp, color = Color.Blue, lineHeight = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        if (cell.reminder != ReminderUiModel.NONE) {
            Text(
                text = if (cell.reminder == ReminderUiModel.MINE) "M?" else "K?",
                modifier = Modifier.align(Alignment.TopStart).background(Color.White.copy(alpha = 0.85f)),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (cell.reminder == ReminderUiModel.MINE) Color.Red else Color.Blue
            )
        }
    }
}
