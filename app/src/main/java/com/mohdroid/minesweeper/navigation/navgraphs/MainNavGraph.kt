package com.mohdroid.minesweeper.navigation.navgraphs

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import com.mohdroid.minesweeper.feature.game.GameRoute
import com.mohdroid.minesweeper.feature.level.LevelRoute
import com.mohdroid.minesweeper.navigation.LocalNavigation
import com.mohdroid.minesweeper.navigation.MsNavigation

@Composable
internal fun MainNavGraph() {
    val navHost = LocalNavigation.current
    NavHost(
        navController = navHost,
        startDestination = MsNavigation.Main.ChooseLevel.routeName()
    ) {
        composable(MsNavigation.Main.ChooseLevel.routeName()) {
            LevelRoute(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(top = 40.dp, start = 24.dp, end = 24.dp, bottom = 50.dp),
                onLevelSelected = { levelId ->
                    navHost.navigate(MsNavigation.Main.BoardGame.routeFor(levelId))
                }
            )
        }
        composable(
            route = MsNavigation.Main.BoardGame.routeName(),
            arguments = listOf(navArgument(MsNavigation.Main.BoardGame.LEVEL_ID_ARGUMENT) {
                type = NavType.StringType
            })
        ) { entry ->
            val levelId = requireNotNull(
                entry.arguments?.getString(MsNavigation.Main.BoardGame.LEVEL_ID_ARGUMENT)
            )
            GameRoute(
                levelId = levelId,
                onExit = { navHost.popBackStack() },
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)
            )
        }
    }
}
