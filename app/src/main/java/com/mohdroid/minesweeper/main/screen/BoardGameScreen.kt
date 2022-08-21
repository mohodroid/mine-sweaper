package com.mohdroid.minesweeper.main.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohdroid.algorithm.DirectionalOfMovement
import com.mohdroid.algorithm.MineSweeper
import com.mohdroid.algorithm.MissionCoordinator
import com.mohdroid.compose.component.Space
import com.mohdroid.compose.theme.MsShapes
import com.mohdroid.compose.theme.MsTypography
import com.mohdroid.compose.theme.gray
import com.mohdroid.minesweeper.R
import com.mohdroid.minesweeper.main.compose.MineSweeperCell
import com.mohdroid.minesweeper.main.compose.MineSweeperControllers
import com.mohdroid.minesweeper.main.viewmodel.FinishGame

var board: List<List<MineSweeper.Node>> = arrayListOf()

@Composable
fun BoardGameScreen(
    modifier: Modifier = Modifier,
    nodes: List<List<MineSweeper.Node>>?,
    cols: Int,
    row: Int,
    gameState: MissionCoordinator.PlayerState,
    totalKeys: Int,
    foundKeys: Int,
    mines: Int,
    isFinished: FinishGame,
    onMovement: (DirectionalOfMovement) -> Unit,
    onTryAgainClick: () -> Unit,
    onExitClicked: () -> Unit
) {
    Column(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(shape = MsShapes.large, color = gray)
                    .wrapContentSize(),
                contentAlignment = Alignment.Center,
            ) {
                Row(modifier = Modifier.padding(8.dp)) {
                    Image(
                        modifier = Modifier
                            .size(20.dp)
                            .padding(top = 1.dp),
                        painter = painterResource(id = R.drawable.ic_mine),
                        contentDescription = null
                    )
                    Text(
                        text = mines.toString(),
                        style = MsTypography.body1.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    )
                }
            }
            Space(size = 16.dp)

            Box(
                modifier = Modifier
                    .background(shape = MsShapes.large, color = gray)
                    .wrapContentSize(),
                contentAlignment = Alignment.Center,
            ) {
                Row(modifier = Modifier.padding(10.dp)) {
                    repeat(foundKeys) {
                        Image(
                            modifier = Modifier
                                .size(20.dp)
                                .padding(top = 1.dp),
                            painter = painterResource(id = R.drawable.ic_key),
                            contentDescription = null
                        )
                    }
                    repeat(totalKeys.minus(foundKeys)) {
                        Image(
                            modifier = Modifier
                                .size(20.dp)
                                .padding(top = 1.dp),
                            painter = painterResource(id = R.drawable.ic_found_key),
                            contentDescription = null
                        )
                    }
                }
            }

        }
        Space(size = 32.dp)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            BoxWithConstraints(
                Modifier.fillMaxHeight()
            ) {
                val width = maxWidth / row
                val height = maxWidth / cols
                nodes?.let {
                    Row {
                        repeat(cols) { repeatedColumn ->
                            Column {
                                repeat(row) { repeatedRow ->
                                    val item = it[repeatedRow][repeatedColumn].item
                                    MineSweeperCell(
                                        modifier = Modifier.size(width, height),
                                        item = item,
                                        state = gameState
                                    )
                                }
                            }
                        }
                    }
                }

            }
        }
        Space(size = 32.dp)
        MineSweeperControllers(
            Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
        ) {
            onMovement(it)
        }
        Space(size = 24.dp)
        if (isFinished.isFinished) {
            FinishGameDialog(modifier = Modifier.padding(8.dp), finishGame = isFinished,
                onTryAgainClick = {
                    onTryAgainClick()
                }) {
                onExitClicked()
            }
        }
    }
}