package com.mohdroid.minesweeper.feature.game.component

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
import com.mohdroid.minesweeper.feature.game.model.MoveDirectionUi
import com.mohdroid.compose.component.SimpleButton
import com.mohdroid.compose.theme.MsTheme
import com.mohdroid.minesweeper.R

@Composable
fun MovementControls(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onClicked: (MoveDirectionUi) -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LeftControl(enabled = enabled, onClicked = onClicked)
        TopControl(enabled = enabled, onClicked = onClicked)
        DownControl(enabled = enabled, onClicked = onClicked)
        RightControl(enabled = enabled, onClicked = onClicked)
    }
}

@Composable
@Preview
fun MovementControlsPrev() {
    MsTheme {
        MovementControls(Modifier.fillMaxWidth(), enabled = true) {

        }
    }
}

@Composable
fun LeftControl(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onClicked: (MoveDirectionUi) -> Unit
) {
    MineSweeperControl(
        modifier = modifier,
        painter = painterResource(id = R.drawable.ic_left_contoller), enabled = enabled, label = "Move left",
        onClicked = {
            onClicked(MoveDirectionUi.LEFT)
        })

}


@Composable
fun TopControl(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onClicked: (MoveDirectionUi) -> Unit
) {
    MineSweeperControl(
        modifier = modifier,
        painter = painterResource(id = R.drawable.ic_up_controller), enabled = enabled, label = "Move up",
        onClicked = {
            onClicked(MoveDirectionUi.UP)
        })
}


@Composable
fun RightControl(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onClicked: (MoveDirectionUi) -> Unit
) {
    MineSweeperControl(
        modifier = modifier,
        painter = painterResource(id = R.drawable.ic_right_controller), enabled = enabled, label = "Move right",
        onClicked = {
            onClicked(MoveDirectionUi.RIGHT)
        })
}

@Composable
fun DownControl(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onClicked: (MoveDirectionUi) -> Unit
) {
    MineSweeperControl(
        modifier = modifier,
        painter = painterResource(id = R.drawable.ic_bottom_contoller), enabled = enabled, label = "Move down",
        onClicked = {
            onClicked(MoveDirectionUi.DOWN)
        })
}

@Composable
fun MineSweeperControl(
    modifier: Modifier = Modifier,
    onClicked: () -> Unit,
    painter: Painter,
    enabled: Boolean,
    label: String
) {
    SimpleButton(
        modifier = modifier.size(48.dp),
        enable = enabled,
        accessibilityLabel = label,
        onClicked = onClicked,
        content = {
            Image(
                modifier = Modifier.size(36.dp),
                contentScale = ContentScale.FillBounds,
                painter = painter,
                contentDescription = label
            )
        })
}
