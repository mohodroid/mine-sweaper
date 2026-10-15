package com.mohdroid.domain.game.usecase.interaction

import com.mohdroid.domain.game.model.board.Direction
import com.mohdroid.domain.game.model.session.GameState
import com.mohdroid.domain.game.model.board.Position
import com.mohdroid.domain.game.model.board.Reminder

interface GameInteraction {
    fun movePlayer(direction: Direction): Result<GameState>
    /** Choosing the existing non-empty reminder toggles it off, matching the game rules. */
    fun setCellReminder(position: Position, reminder: Reminder): Result<GameState>
}
