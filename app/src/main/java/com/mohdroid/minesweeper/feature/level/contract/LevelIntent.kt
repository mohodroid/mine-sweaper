package com.mohdroid.minesweeper.feature.level.contract

sealed interface LevelIntent {
    object Load : LevelIntent
    object Retry : LevelIntent
    data class SelectLevel(val levelId: String) : LevelIntent
    data class NavigationHandled(val navigation: LevelNavigation) : LevelIntent
    object ScreenResumed : LevelIntent
}
