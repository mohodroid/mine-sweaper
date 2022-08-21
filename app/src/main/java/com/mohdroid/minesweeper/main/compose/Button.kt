package com.mohdroid.minesweeper.main.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mohdroid.compose.component.SimpleButton
import com.mohdroid.compose.theme.MsTheme
import com.mohdroid.compose.theme.bottomStyle
import com.mohdroid.compose.theme.dialogBottomStyle
import com.mohdroid.minesweeper.R


@Composable
fun SimpleChooseLevelButton(
    modifier: Modifier = Modifier,
    enable: Boolean = true,
    onClicked: () -> Unit,
    painter: Painter = painterResource(id = R.drawable.ic_button_background),
    content: @Composable () -> Unit
) {
    SimpleButton(modifier = modifier.height(74.dp), content = {
        Image(
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
            painter = painter,
            contentDescription = null
        )
        content()
    }, enable = enable, onClicked = onClicked)
}

@Composable
fun MsBtnEasyLevel(modifier: Modifier = Modifier, enable: Boolean = true, onClicked: () -> Unit) {
    SimpleChooseLevelButton(modifier = modifier, content = {
        Text(text = stringResource(R.string.easy), style = MsTheme.typography.bottomStyle)
    }, enable = enable, onClicked = onClicked)
}

@Composable
fun MsBtnMediumLevel(modifier: Modifier = Modifier, enable: Boolean = true, onClicked: () -> Unit) {
    SimpleChooseLevelButton(modifier = modifier, content = {
        Text(text = stringResource(R.string.medium), style = MsTheme.typography.bottomStyle)
    }, enable = enable, onClicked = onClicked)
}

@Composable
fun MsBtnHardLevel(modifier: Modifier = Modifier, enable: Boolean = true, onClicked: () -> Unit) {
    SimpleChooseLevelButton(modifier = modifier, content = {
        Text(text = stringResource(R.string.hard), style = MsTheme.typography.bottomStyle)
    }, enable = enable, onClicked = onClicked)
}

@Composable
fun MsBtnTryAgain(modifier: Modifier = Modifier, enable: Boolean = true, onClicked: () -> Unit) {
    SimpleChooseLevelButton(modifier = modifier, content = {
        Text(text = stringResource(R.string.try_again), style = MsTheme.typography.dialogBottomStyle)
    }, enable = enable, onClicked = onClicked)
}

@Composable
fun MsBtnExit(modifier: Modifier = Modifier, enable: Boolean = true, onClicked: () -> Unit) {
    SimpleChooseLevelButton(
        painter = painterResource(id = R.drawable.ic_red_btn),
        modifier = modifier,
        content = {
            Text(text = stringResource(R.string.exit_game), style = MsTheme.typography.dialogBottomStyle)
        },
        enable = enable,
        onClicked = onClicked
    )
}
