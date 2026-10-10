package com.mohdroid.minesweeper.main.screen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohdroid.game.domain.Direction
import com.mohdroid.game.domain.FlagKind
import com.mohdroid.game.domain.GamePhase
import com.mohdroid.game.domain.GameSnapshot
import com.mohdroid.game.domain.Position
import com.mohdroid.compose.component.Space
import com.mohdroid.compose.theme.MsTypography
import com.mohdroid.minesweeper.main.compose.MineSweeperCell
import com.mohdroid.minesweeper.main.compose.MineSweeperControllers
import com.mohdroid.minesweeper.main.viewmodel.GameLoadState
import com.mohdroid.minesweeper.main.viewmodel.GameUiState

@Composable
fun BoardGameScreen(
    modifier: Modifier = Modifier,
    state: GameUiState,
    onMovement: (Direction) -> Unit,
    onFlag: (Position, FlagKind) -> Unit,
    onTryAgainClick: () -> Unit,
    onRetryLoadClick: () -> Unit,
    onExitClicked: () -> Unit
) {
    when (state.loadState) {
        GameLoadState.LOADING, GameLoadState.IDLE -> LoadingBoard(modifier)
        GameLoadState.ERROR -> ErrorBoard(modifier, state.errorMessage, onRetryLoadClick)
        GameLoadState.READY -> state.game?.let { game ->
            ReadyBoard(
                modifier = modifier,
                game = game,
                controlsEnabled = state.canMove,
                onMovement = onMovement,
                onFlag = onFlag,
                onTryAgainClick = onTryAgainClick,
                onExitClicked = onExitClicked
            )
        } ?: ErrorBoard(modifier, "The game was not initialized.", onRetryLoadClick)
    }
}

@Composable
private fun LoadingBoard(modifier: Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorBoard(modifier: Modifier, message: String?, onRetry: () -> Unit) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(message ?: "Unable to load this level.")
        Space(16.dp)
        Button(onClick = onRetry) { Text("Try again") }
    }
}

@Composable
private fun ReadyBoard(
    modifier: Modifier,
    game: GameSnapshot,
    controlsEnabled: Boolean,
    onMovement: (Direction) -> Unit,
    onFlag: (Position, FlagKind) -> Unit,
    onTryAgainClick: () -> Unit,
    onExitClicked: () -> Unit
) {
    var selected by remember { mutableStateOf<Position?>(null) }
    var reviewingBoard by remember(game.phase) { mutableStateOf(false) }
    val selectedCell = selected?.let { game.board[it.row][it.column] }
    val flaggableSelection = selected?.takeIf { selectedCell?.tile == null && controlsEnabled }
    Column(modifier) {
        GameStats(game)
        Space(8.dp)
        Board(
            game = game,
            selected = flaggableSelection,
            onCellSelected = { position -> selected = position },
            modifier = Modifier.weight(1f).fillMaxWidth()
        )
        Space(8.dp)
        if (controlsEnabled) {
            if (flaggableSelection == null) {
                Text("Tap a covered cell to mark a mine or key. Flags are reminders.", fontSize = 12.sp)
            } else {
                Text("Cell ${flaggableSelection.row + 1}, ${flaggableSelection.column + 1}", fontSize = 12.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button(
                        onClick = { onFlag(flaggableSelection, FlagKind.MINE) },
                        modifier = Modifier.weight(1f)
                    ) { Text("Mine flag", fontSize = 11.sp) }
                    Button(
                        onClick = { onFlag(flaggableSelection, FlagKind.KEY) },
                        modifier = Modifier.weight(1f)
                    ) { Text("Key flag", fontSize = 11.sp) }
                    Button(
                        onClick = { onFlag(flaggableSelection, FlagKind.NONE) },
                        modifier = Modifier.weight(1f),
                        enabled = selectedCell?.flag != FlagKind.NONE
                    ) { Text("Clear", fontSize = 11.sp) }
                }
            }
        }
        Space(8.dp)
        MineSweeperControllers(
            modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally),
            enabled = controlsEnabled,
            onClicked = onMovement
        )
        Space(8.dp)
        if (game.phase != GamePhase.PLAYING) {
            if (reviewingBoard) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onTryAgainClick, modifier = Modifier.weight(1f)) { Text("Try again") }
                    Button(onClick = onExitClicked, modifier = Modifier.weight(1f)) { Text("Exit") }
                }
            } else {
                FinishGameDialog(
                    modifier = Modifier.padding(8.dp),
                    isWin = game.phase == GamePhase.WON,
                    onTryAgainClick = onTryAgainClick,
                    onExitClocked = onExitClicked,
                    onReviewClick = { reviewingBoard = true }
                )
            }
        }
    }
}

@Composable
private fun GameStats(game: GameSnapshot) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Mines: ${game.mines}", style = MsTypography.body1)
            Text("Keys: ${game.foundKeys}/${game.totalKeys}", style = MsTypography.body1)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Lives: ${game.remainingLives}/${game.startingLives}", style = MsTypography.body1)
            Text("Moves: ${game.moves}", style = MsTypography.body1)
        }
    }
}

@Composable
private fun Board(
    game: GameSnapshot,
    selected: Position?,
    onCellSelected: (Position) -> Unit,
    modifier: Modifier
) {
    BoxWithConstraints(modifier, contentAlignment = Alignment.TopCenter) {
        val cellWidth = maxWidth / game.size.columns
        val cellHeight = maxHeight / game.size.rows
        val cellSize = maxOf(32.dp, minOf(cellWidth, cellHeight))
        Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Column(Modifier.horizontalScroll(rememberScrollState())) {
                game.board.forEachIndexed { row, cells ->
                    Row {
                        cells.forEachIndexed { column, cell ->
                            val position = Position(row, column)
                            MineSweeperCell(
                                modifier = Modifier.size(cellSize),
                                cell = cell,
                                isPlayer = game.player == position,
                                phase = game.phase,
                                exitUnlocked = game.exitUnlocked,
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
