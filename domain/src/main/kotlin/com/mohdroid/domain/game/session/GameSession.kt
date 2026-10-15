package com.mohdroid.domain.game.session

import com.mohdroid.domain.game.model.board.Direction
import com.mohdroid.domain.game.model.board.Position
import com.mohdroid.domain.game.model.board.Reminder
import com.mohdroid.domain.game.model.level.Difficulty
import com.mohdroid.domain.game.model.level.LevelConfiguration
import com.mohdroid.domain.game.model.session.GameState

/** Domain-only session port used by use-case implementations, never directly by presentation. */
internal interface GameSession {
    /** Discards the active game and invalidates older starts before level loading begins. */
    fun beginStart(difficulty: Difficulty): GameStartRequest

    /**
     * Creates and installs a fresh game only while the request and caller remain active.
     * Ordinary creation failures use Result; cancellation and stale starts throw cancellation.
     */
    suspend fun activate(
        request: GameStartRequest,
        configuration: LevelConfiguration
    ): Result<GameState>

    /** Throws cancellation if this start was replaced or the game was exited. */
    fun ensureCurrent(request: GameStartRequest)

    fun movePlayer(direction: Direction): Result<GameState>

    /** Reminders retain the engine's existing set/change/toggle/clear behavior. */
    fun setCellReminder(position: Position, reminder: Reminder): Result<GameState>

    /** Idempotently clears the active game and invalidates unfinished starts. */
    fun exit()
}
