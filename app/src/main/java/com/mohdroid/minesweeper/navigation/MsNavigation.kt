package com.mohdroid.minesweeper.navigation

sealed class MsNavigation(private val route: String) {
    open fun routeName(): String = "minesweeper/$route"

    sealed class Main(private val route: String) : MsNavigation(route) {

        override fun routeName(): String = "minesweeper/main/$route"

        companion object {
            const val routeName = "Main"
        }

        object ChooseLevel : Main("choose_level")

        object BoardGame : Main("board_game")

    }


}