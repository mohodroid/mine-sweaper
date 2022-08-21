package com.mohdroid.minesweeper.main.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohdroid.compose.component.Space
import com.mohdroid.minesweeper.main.DifficultyLevel
import com.mohdroid.minesweeper.main.compose.MsBtnEasyLevel
import com.mohdroid.minesweeper.main.compose.MsBtnHardLevel
import com.mohdroid.minesweeper.main.compose.MsBtnMediumLevel
import com.mohdroid.minesweeper.main.compose.WelcomeAndLogo

@Composable
fun ChooseLevelScreen(modifier: Modifier = Modifier, onBtnClicked: (DifficultyLevel) -> Unit) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        WelcomeAndLogo()
        Space(40.dp)
        MsBtnEasyLevel(modifier = Modifier.fillMaxWidth()) {
            onBtnClicked(DifficultyLevel.EASY)
        }
        Space(40.dp)
        MsBtnMediumLevel(modifier = Modifier.fillMaxWidth()) {
            onBtnClicked(DifficultyLevel.MEDIUM)
        }
        Space(40.dp)
        MsBtnHardLevel(modifier = Modifier.fillMaxWidth()) {
            onBtnClicked(DifficultyLevel.HARD)
        }
    }
}