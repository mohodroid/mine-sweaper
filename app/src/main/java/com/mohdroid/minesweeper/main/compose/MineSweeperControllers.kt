package com.mohdroid.minesweeper.main.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mohdroid.algorithm.DirectionalOfMovement
import com.mohdroid.compose.component.SimpleButton
import com.mohdroid.compose.component.Space
import com.mohdroid.compose.theme.MsTheme
import com.mohdroid.minesweeper.R

@Composable
fun MineSweeperControllers(
    modifier: Modifier = Modifier,
    onClicked: (DirectionalOfMovement) -> Unit
) {
    Column(modifier, verticalArrangement = Arrangement.Center) {
        TopControl(modifier = Modifier.align(Alignment.CenterHorizontally), onClicked = onClicked)
        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            LeftControl(
                modifier = Modifier.align(Alignment.CenterVertically),
                onClicked = onClicked
            )
            Space(size = 48.dp)
            RightControl(
                modifier = Modifier.align(Alignment.CenterVertically),
                onClicked = onClicked
            )
        }
        DownControl(modifier = Modifier.align(Alignment.CenterHorizontally), onClicked = onClicked)
    }
}

@Composable
@Preview
fun MineSweeperControllersPrev() {
    MsTheme {
        MineSweeperControllers(Modifier.fillMaxWidth()) {

        }
    }
}

@Composable
fun LeftControl(
    modifier: Modifier = Modifier,
    onClicked: (DirectionalOfMovement) -> Unit
) {
    MineSweeperControl(
        modifier = modifier,
        painter = painterResource(id = R.drawable.ic_left_contoller),
        onClicked = {
            onClicked(DirectionalOfMovement.WEST)
        })

}


@Composable
fun TopControl(
    modifier: Modifier = Modifier,
    onClicked: (DirectionalOfMovement) -> Unit
) {
    MineSweeperControl(
        modifier = modifier,
        painter = painterResource(id = R.drawable.ic_up_controller),
        onClicked = {
            onClicked(DirectionalOfMovement.NORTH)
        })
}


@Composable
fun RightControl(
    modifier: Modifier = Modifier,
    onClicked: (DirectionalOfMovement) -> Unit
) {
    MineSweeperControl(
        modifier = modifier,
        painter = painterResource(id = R.drawable.ic_right_controller),
        onClicked = {
            onClicked(DirectionalOfMovement.EAST)
        })
}

@Composable
fun DownControl(
    modifier: Modifier = Modifier,
    onClicked: (DirectionalOfMovement) -> Unit
) {
    MineSweeperControl(
        modifier = modifier,
        painter = painterResource(id = R.drawable.ic_bottom_contoller),
        onClicked = {
            onClicked(DirectionalOfMovement.SOUTH)
        })
}

@Composable
fun MineSweeperControl(
    modifier: Modifier = Modifier,
    onClicked: () -> Unit,
    painter: Painter
) {
    SimpleButton(
        modifier = modifier.size(48.dp),
        onClicked = onClicked,
        content = {
            Image(
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
                painter = painter,
                contentDescription = null
            )
        })
}