package com.mohdroid.domain.game.usecase.interaction

import com.mohdroid.domain.game.model.board.Direction
import com.mohdroid.domain.game.model.board.Position
import com.mohdroid.domain.game.model.board.Reminder
import com.mohdroid.domain.game.model.session.GameState
import com.mohdroid.domain.game.session.GameSession

internal class DefaultGameInteraction(private val session: GameSession) : GameInteraction {
    override fun movePlayer(direction: Direction): Result<GameState> =
        session.movePlayer(direction)

    override fun setCellReminder(position: Position, reminder: Reminder): Result<GameState> =
        session.setCellReminder(position, reminder)
}
