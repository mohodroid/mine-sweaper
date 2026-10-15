package com.mohdroid.minesweeper.feature.game

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohdroid.minesweeper.R
import com.mohdroid.minesweeper.ui.error.UiError
import com.mohdroid.minesweeper.ui.error.message
import com.mohdroid.compose.component.Space
import com.mohdroid.minesweeper.feature.game.component.FinishGameDialog
import com.mohdroid.minesweeper.feature.game.component.GameBoard
import com.mohdroid.minesweeper.feature.game.component.GameStats
import com.mohdroid.minesweeper.feature.game.component.MovementControls
import com.mohdroid.minesweeper.feature.game.contract.GameIntent
import com.mohdroid.minesweeper.feature.game.contract.GameLoadState
import com.mohdroid.minesweeper.feature.game.contract.GameUiState
import com.mohdroid.minesweeper.feature.game.model.CellPositionUiModel
import com.mohdroid.minesweeper.feature.game.model.GameStatusUiModel
import com.mohdroid.minesweeper.feature.game.model.MoveDirectionUi
import com.mohdroid.minesweeper.feature.game.model.ReminderUiModel

/** Rendering only: no ViewModel, Koin or navigation dependencies. */
@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    state: GameUiState,
    onIntent: (GameIntent) -> Unit
) {
    when (state.loadState) {
        GameLoadState.LOADING, GameLoadState.IDLE -> LoadingBoard(modifier)
        GameLoadState.ERROR -> ErrorBoard(modifier, state.error ?: UiError.GAME_LOADING_FAILED, onIntent)
        GameLoadState.READY -> if (state.game != null) {
            ReadyBoard(modifier, state, onIntent)
        } else ErrorBoard(modifier, UiError.GAME_LOADING_FAILED, onIntent)
    }
}

@Composable
private fun LoadingBoard(modifier: Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorBoard(modifier: Modifier, error: UiError, onIntent: (GameIntent) -> Unit) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(error.message())
        Space(16.dp)
        Button(onClick = { onIntent(if (error.canRetry) GameIntent.RetryLoad else GameIntent.Exit) }) {
            Text(stringResource(if (error.canRetry) R.string.retry_loading else R.string.back_to_levels))
        }
    }
}

@Composable
private fun ReadyBoard(
    modifier: Modifier,
    state: GameUiState,
    onIntent: (GameIntent) -> Unit
) {
    val game = checkNotNull(state.game)
    val controlsEnabled = state.canMove
    val selected = state.selectedCell
    val reviewingBoard = state.isReviewingBoard
    val onMovement: (MoveDirectionUi) -> Unit = { onIntent(GameIntent.Move(it)) }
    val onFlag: (CellPositionUiModel, ReminderUiModel) -> Unit = { position, reminder ->
        onIntent(GameIntent.SetReminder(position, reminder))
    }
    val onTryAgainClick = { onIntent(GameIntent.Restart) }
    val onExitClicked = { onIntent(GameIntent.Exit) }
    val selectedCell = selected?.let { game.cells.getOrNull(it.row)?.getOrNull(it.column) }
    val flaggableSelection = selected?.takeIf { selectedCell?.canSetReminder == true && controlsEnabled }
    Column(modifier) {
        GameStats(game)
        Space(8.dp)
        GameBoard(
            game = game,
            selected = flaggableSelection,
            onCellSelected = { position -> onIntent(GameIntent.SelectCell(position)) },
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
                        onClick = { onFlag(flaggableSelection, ReminderUiModel.MINE) },
                        modifier = Modifier.weight(1f)
                    ) { Text("Mine flag", fontSize = 11.sp) }
                    Button(
                        onClick = { onFlag(flaggableSelection, ReminderUiModel.KEY) },
                        modifier = Modifier.weight(1f)
                    ) { Text("Key flag", fontSize = 11.sp) }
                    Button(
                        onClick = { onFlag(flaggableSelection, ReminderUiModel.NONE) },
                        modifier = Modifier.weight(1f),
                        enabled = selectedCell?.reminder != ReminderUiModel.NONE
                    ) { Text("Clear", fontSize = 11.sp) }
                }
            }
        }
        Space(8.dp)
        MovementControls(
            modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally),
            enabled = controlsEnabled,
            onClicked = onMovement
        )
        Space(8.dp)
        if (game.status != GameStatusUiModel.IN_PROGRESS) {
            if (reviewingBoard) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onTryAgainClick, modifier = Modifier.weight(1f)) { Text("Try again") }
                    Button(onClick = onExitClicked, modifier = Modifier.weight(1f)) { Text("Exit") }
                }
            } else {
                FinishGameDialog(
                    modifier = Modifier.padding(8.dp),
                    isWin = game.status == GameStatusUiModel.WON,
                    onTryAgainClick = onTryAgainClick,
                    onExitClick = onExitClicked,
                    onReviewClick = { onIntent(GameIntent.ReviewBoard) }
                )
            }
        }
    }
}
