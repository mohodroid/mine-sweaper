package com.mohdroid.minesweeper.feature.level

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import com.mohdroid.minesweeper.feature.level.contract.LevelIntent
import com.mohdroid.minesweeper.ui.lifecycle.collectAsStateWithLifecycle
import com.mohdroid.minesweeper.ui.lifecycle.collectWhileActive
import org.koin.androidx.compose.getViewModel

@Composable
internal fun LevelRoute(
    onLevelSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = getViewModel<LevelViewModel>()
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val navigate = rememberUpdatedState(onLevelSelected)

    LaunchedEffect(viewModel, lifecycle) {
        viewModel.uiState.collectWhileActive(
            lifecycle = lifecycle,
            minActiveState = Lifecycle.State.RESUMED,
            onActive = { viewModel.onIntent(LevelIntent.ScreenResumed) }
        ) { current ->
            current.pendingNavigation?.let { navigation ->
                navigate.value(navigation.levelId)
                viewModel.onIntent(LevelIntent.NavigationHandled(navigation))
            }
        }
    }

    LevelScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}
