package com.mohdroid.minesweeper.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mohdroid.minesweeper.navigation.navgraphs.MainNavGraph

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun MsNavGraph() {

    val startDest = MsNavigation.Main.routeName

    ProvideNavigation {

        NavHost(navController = LocalNavigation.current, startDestination = startDest) {
            composable(startDest) {
                MainNavGraph()
            }
        }
    }
}


