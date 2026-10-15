package com.mohdroid.minesweeper.feature.game

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import com.mohdroid.minesweeper.feature.game.contract.GameIntent
import com.mohdroid.minesweeper.feature.game.contract.GameNavigation
import com.mohdroid.minesweeper.ui.lifecycle.collectAsStateWithLifecycle
import com.mohdroid.minesweeper.ui.lifecycle.collectWhileActive
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf

/** Owns the destination's ViewModel and navigation; GameScreen only renders state. */
@Composable
internal fun GameRoute(
    levelId: String,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = getViewModel<GameViewModel>(parameters = { parametersOf(levelId) })
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val navigateBack = rememberUpdatedState(onExit)

    LaunchedEffect(viewModel, lifecycle) {
        viewModel.uiState.collectWhileActive(lifecycle, Lifecycle.State.RESUMED) { current ->
            current.pendingNavigation?.let { navigation ->
                when (navigation) {
                    GameNavigation.BackToLevels -> navigateBack.value()
                }
                viewModel.onIntent(GameIntent.NavigationHandled(navigation))
            }
        }
    }

    BackHandler { viewModel.onIntent(GameIntent.Exit) }
    GameScreen(modifier = modifier, state = state, onIntent = viewModel::onIntent)
}
