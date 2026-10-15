package com.mohdroid.domain.game.usecase.start

import com.mohdroid.domain.game.model.level.Difficulty
import com.mohdroid.domain.game.model.session.GameState

interface StartGameUseCase {
    /** Starts fresh; also used for restart and retrying a failed load. */
    suspend operator fun invoke(difficulty: Difficulty): Result<GameState>
}
