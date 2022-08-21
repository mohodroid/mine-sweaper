package com.mohdroid.minesweeper.main.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohdroid.compose.component.Space
import com.mohdroid.compose.theme.MsTheme
import com.mohdroid.compose.theme.red
import com.mohdroid.minesweeper.R

@Composable
fun WelcomeAndLogo(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(35.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = painterResource(id = R.drawable.ic_welcome_), contentDescription = null)
    }
}


@Preview
@Composable
fun WelcomeAndLogoPreview() {
    MsTheme {
        WelcomeAndLogo(modifier = Modifier.padding(20.dp))
    }
}