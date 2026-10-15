package com.mohdroid.minesweeper.feature.game.contract

import com.mohdroid.minesweeper.feature.game.model.CellPositionUiModel
import com.mohdroid.minesweeper.feature.game.model.MoveDirectionUi
import com.mohdroid.minesweeper.feature.game.model.ReminderUiModel

sealed interface GameIntent {
    object Start : GameIntent
    data class Move(val direction: MoveDirectionUi) : GameIntent
    data class SetReminder(val position: CellPositionUiModel, val reminder: ReminderUiModel) : GameIntent
    data class SelectCell(val position: CellPositionUiModel) : GameIntent
    object ReviewBoard : GameIntent
    object Restart : GameIntent
    object RetryLoad : GameIntent
    object Exit : GameIntent
    data class NavigationHandled(val navigation: GameNavigation) : GameIntent
}
