package com.mohdroid.minesweeper.feature.game.component

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.mohdroid.compose.theme.MsTheme
import com.mohdroid.compose.theme.dialogBottomStyle
import com.mohdroid.minesweeper.R
import com.mohdroid.minesweeper.ui.component.IllustratedButton

@Composable
internal fun TryAgainButton(modifier: Modifier = Modifier, enable: Boolean = true, onClicked: () -> Unit) {
    IllustratedButton(modifier = modifier, enable = enable, onClicked = onClicked) {
        Text(stringResource(R.string.try_again), style = MsTheme.typography.dialogBottomStyle)
    }
}

@Composable
internal fun ExitGameButton(modifier: Modifier = Modifier, enable: Boolean = true, onClicked: () -> Unit) {
    IllustratedButton(
        modifier = modifier, enable = enable, onClicked = onClicked,
        painter = painterResource(R.drawable.ic_red_btn)
    ) {
        Text(stringResource(R.string.exit_game), style = MsTheme.typography.dialogBottomStyle)
    }
}
