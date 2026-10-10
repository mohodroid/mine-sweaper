package com.mohdroid.minesweeper.navigation.navgraphs

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import com.mohdroid.minesweeper.main.screen.BoardGameScreen
import com.mohdroid.minesweeper.main.screen.ChooseLevelScreen
import com.mohdroid.minesweeper.main.viewmodel.GameUiState
import com.mohdroid.minesweeper.main.viewmodel.MainViewModel
import com.mohdroid.minesweeper.navigation.MsNavigation
import com.mohdroid.minesweeper.navigation.LocalNavigation
import org.koin.androidx.compose.getViewModel

@Composable
internal fun MainNavGraph() {
    val navHost = LocalNavigation.current
    val vm = getViewModel<MainViewModel>()
    NavHost(
        navController = navHost,
        startDestination = MsNavigation.Main.ChooseLevel.routeName()
    ) {
            composable(MsNavigation.Main.ChooseLevel.routeName()) {
                ChooseLevelScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(top = 40.dp, start = 24.dp, end = 24.dp, bottom = 50.dp),
                    onBtnClicked = {
                        vm.onLevel(it)
                        navHost.navigate(MsNavigation.Main.BoardGame.routeName())
                    })
            }
            composable(MsNavigation.Main.BoardGame.routeName()) {
                val gameState = vm.uiState.collectAsState(initial = GameUiState()).value
                BackHandler {
                    navHost.popBackStack()
                }
                BoardGameScreen(modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
                    state = gameState,
                    onMovement = vm::onMovement,
                    onFlag = vm::onFlag,
                    onExitClicked = {
                        navHost.popBackStack()
                    },
                    onTryAgainClick = {
                        vm.onTryAgain()
                    },
                    onRetryLoadClick = vm::onRetryLoad)
            }
    }
}
