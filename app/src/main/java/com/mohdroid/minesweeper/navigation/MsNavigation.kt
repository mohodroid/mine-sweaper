package com.mohdroid.minesweeper.navigation

import android.net.Uri

sealed class MsNavigation(private val route: String) {
    open fun routeName(): String = "minesweeper/$route"

    sealed class Main(private val route: String) : MsNavigation(route) {
        override fun routeName(): String = "minesweeper/main/$route"

        companion object {
            const val routeName = "Main"
        }

        object ChooseLevel : Main("choose_level")

        object BoardGame : Main("board_game") {
            const val LEVEL_ID_ARGUMENT = "levelId"

            override fun routeName(): String = "${super.routeName()}/{$LEVEL_ID_ARGUMENT}"

            fun routeFor(levelId: String): String = "${super.routeName()}/${Uri.encode(levelId)}"
        }
    }
}
