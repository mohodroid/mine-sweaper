package com.mohdroid.minesweeper.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController


val LocalNavigation = compositionLocalOf<NavHostController> { error("") }

@Composable
fun ProvideNavigation(
    navHostController: NavHostController = rememberNavController(),
    content: @Composable (NavHostController) -> Unit

) {
    CompositionLocalProvider(LocalNavigation provides navHostController) {
        content(navHostController)
    }
}
