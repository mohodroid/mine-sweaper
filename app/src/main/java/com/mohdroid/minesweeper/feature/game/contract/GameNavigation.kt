package com.mohdroid.minesweeper.feature.game.contract

/** Retained until the resumed route handles and acknowledges the request. */
sealed interface GameNavigation {
    object BackToLevels : GameNavigation
}
