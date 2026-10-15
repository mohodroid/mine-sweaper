package com.mohdroid.minesweeper.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mohdroid.compose.component.SimpleButton
import com.mohdroid.minesweeper.R

/** App-branded visual shared by level choices and game-dialog buttons. */
@Composable
internal fun IllustratedButton(
    modifier: Modifier = Modifier,
    enable: Boolean = true,
    onClicked: () -> Unit,
    painter: Painter = painterResource(R.drawable.ic_button_background),
    content: @Composable () -> Unit
) {
    SimpleButton(
        modifier = modifier.height(74.dp),
        enable = enable,
        onClicked = onClicked,
        content = {
            Image(
                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.FillBounds,
                painter = painter, contentDescription = null
            )
            content()
        }
    )
}
