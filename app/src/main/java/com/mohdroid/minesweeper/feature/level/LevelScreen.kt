package com.mohdroid.minesweeper.feature.level

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mohdroid.minesweeper.R
import com.mohdroid.minesweeper.ui.error.UiError
import com.mohdroid.minesweeper.ui.error.message
import com.mohdroid.compose.component.Space
import com.mohdroid.minesweeper.feature.level.component.LevelButton
import com.mohdroid.minesweeper.feature.level.component.WelcomeAndLogo
import com.mohdroid.minesweeper.feature.level.contract.LevelIntent
import com.mohdroid.minesweeper.feature.level.contract.LevelLoadState
import com.mohdroid.minesweeper.feature.level.contract.LevelUiState

@Composable
fun LevelScreen(
    state: LevelUiState,
    onIntent: (LevelIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    when (state.loadState) {
        LevelLoadState.LOADING -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        LevelLoadState.ERROR -> Column(
            modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text((state.error ?: UiError.LEVEL_LOADING_FAILED).message())
            Space(16.dp)
            Button(onClick = { onIntent(LevelIntent.Retry) }) { Text(stringResource(R.string.retry_loading)) }
        }
        LevelLoadState.READY -> Column(
            modifier.verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            WelcomeAndLogo()
            state.levels.forEach { level ->
                Space(24.dp)
                LevelButton(
                    level = level, enabled = !state.isNavigating,
                    onClick = { onIntent(LevelIntent.SelectLevel(level.id)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (state.levels.isEmpty()) Text("No levels are available.")
        }
    }
}
