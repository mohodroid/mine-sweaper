package com.mohdroid.minesweeper.main.compose

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
import com.mohdroid.game.domain.Cell
import com.mohdroid.game.domain.FlagKind
import com.mohdroid.game.domain.GamePhase
import com.mohdroid.game.domain.TileKind
import com.mohdroid.minesweeper.R

@Composable
private fun cellPainter(
    cell: Cell,
    isPlayer: Boolean,
    phase: GamePhase,
    exitUnlocked: Boolean
): Painter = when {
    isPlayer -> when (phase) {
        GamePhase.WON -> painterResource(R.drawable.ic_cell_happy_s)
        GamePhase.LOST -> painterResource(R.drawable.ic_cell_sad_s)
        GamePhase.PLAYING -> painterResource(R.drawable.ic_cell_soldier)
    }
    cell.tile == null -> painterResource(R.drawable.ic_cell__2_)
    cell.tile == TileKind.START -> if (cell.visited) {
        painterResource(R.drawable.ic_cell_walked)
    } else {
        painterResource(R.drawable.ic_cell_soldier)
    }
    cell.tile == TileKind.EXIT -> if (exitUnlocked) {
        painterResource(R.drawable.ic_cell_open_door)
    } else {
        painterResource(R.drawable.ic_cell_close_door)
    }
    cell.tile == TileKind.MINE -> painterResource(R.drawable.ic_cell_mine)
    cell.tile == TileKind.KEY -> if (cell.visited) {
        painterResource(R.drawable.ic_cell_key_founded)
    } else {
        painterResource(R.drawable.ic_cell_key)
    }
    cell.tile == TileKind.BLOCK -> painterResource(R.drawable.ic_cell_block)
    cell.visited -> painterResource(R.drawable.ic_cell_walked)
    else -> painterResource(R.drawable.ic_cell__2_)
}

@Composable
fun MineSweeperCell(
    modifier: Modifier = Modifier,
    cell: Cell,
    isPlayer: Boolean,
    phase: GamePhase,
    exitUnlocked: Boolean,
    selected: Boolean,
    row: Int,
    column: Int,
    onClick: () -> Unit
) {
    val flagLabel = when (cell.flag) {
        FlagKind.NONE -> ""
        FlagKind.MINE -> ", suspected mine"
        FlagKind.KEY -> ", suspected key"
    }
    val clueLabel = if (cell.adjacentMines != null && cell.adjacentKeys != null) {
        ", adjacent mines ${cell.adjacentMines}, adjacent keys ${cell.adjacentKeys}"
    } else ""
    val label = "Row ${row + 1}, column ${column + 1}, " +
        (if (isPlayer) "player on " else "") +
        (cell.tile?.name?.lowercase() ?: "covered cell") + flagLabel + clueLabel
    Box(
        modifier = modifier
            .padding(0.2.dp)
            .then(if (selected) Modifier.border(2.dp, Color.Yellow) else Modifier)
            .clickable(enabled = cell.tile == null && phase == GamePhase.PLAYING, onClick = onClick)
            .semantics { contentDescription = label }
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = cellPainter(cell, isPlayer, phase, exitUnlocked),
            contentScale = ContentScale.FillBounds,
            contentDescription = null
        )
        if (cell.adjacentMines != null && cell.adjacentKeys != null) {
            Column(
                modifier = Modifier.align(Alignment.BottomEnd).background(Color.White.copy(alpha = 0.82f)),
                horizontalAlignment = Alignment.End
            ) {
                Text("M${cell.adjacentMines}", fontSize = 10.sp, color = Color.Red, lineHeight = 10.sp, fontWeight = FontWeight.Bold)
                Text("K${cell.adjacentKeys}", fontSize = 10.sp, color = Color.Blue, lineHeight = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        if (cell.flag != FlagKind.NONE) {
            Text(
                text = if (cell.flag == FlagKind.MINE) "M?" else "K?",
                modifier = Modifier.align(Alignment.TopStart).background(Color.White.copy(alpha = 0.85f)),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (cell.flag == FlagKind.MINE) Color.Red else Color.Blue
            )
        }
    }
}
