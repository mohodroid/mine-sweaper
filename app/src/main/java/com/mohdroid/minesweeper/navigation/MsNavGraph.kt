package com.mohdroid.minesweeper.navigation

import androidx.compose.runtime.Composable
import com.mohdroid.minesweeper.navigation.navgraphs.MainNavGraph

@Composable
internal fun MsNavGraph() {

    ProvideNavigation {
        MainNavGraph()
    }
}
