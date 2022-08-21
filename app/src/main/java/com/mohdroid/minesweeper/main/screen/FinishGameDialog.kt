package com.mohdroid.minesweeper.main.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.mohdroid.compose.component.Space
import com.mohdroid.compose.theme.MsTypography
import com.mohdroid.compose.theme.green
import com.mohdroid.compose.theme.mediumBlack20
import com.mohdroid.compose.theme.semiBoldRed32
import com.mohdroid.minesweeper.R
import com.mohdroid.minesweeper.main.compose.MsBtnExit
import com.mohdroid.minesweeper.main.compose.MsBtnTryAgain
import com.mohdroid.minesweeper.main.viewmodel.FinishGame

@Composable
fun FinishGameDialog(
    modifier: Modifier = Modifier,
    finishGame: FinishGame,
    onTryAgainClick: () -> Unit,
    onExitClocked: () -> Unit
) {
    Dialog(onDismissRequest = { }) {
        Card(
            elevation = 8.dp,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier.padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Space(size = 24.dp)
                TextHeader(finishGame = finishGame)
                Space(size = 12.dp)
                Text(
                    text = if (finishGame.isWin)
                        stringResource(id = R.string.u_win)
                    else stringResource(id = R.string.u_loose),
                    style = MsTypography.mediumBlack20
                )
                Space(size = 32.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MsBtnExit(
                        modifier = Modifier
                            .weight(1f)
                            .height(35.dp)
                    ) {
                        onExitClocked()
                    }
                    Spacer(modifier = Modifier.weight(0.1f))
                    MsBtnTryAgain(
                        Modifier
                            .weight(1f)
                            .height(35.dp)
                    ) {
                        onTryAgainClick()
                    }

                }
                Space(size = 24.dp)
            }
        }
    }
}

@Composable
fun TextHeader(modifier: Modifier = Modifier, finishGame: FinishGame) {
    val textHeader =
        if (finishGame.isWin) stringResource(id = R.string.congratulations) else stringResource(
            id = R.string.game_finished
        )
    val textStyle =
        if (finishGame.isWin) MsTypography.semiBoldRed32.copy(color = green)
        else MsTypography.semiBoldRed32

    Text(
        textAlign = TextAlign.Center,
        modifier = modifier.then(Modifier.fillMaxWidth()),
        text = textHeader,
        style = textStyle,
    )
}