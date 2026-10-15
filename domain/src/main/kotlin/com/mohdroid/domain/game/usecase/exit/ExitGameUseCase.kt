package com.mohdroid.domain.game.usecase.exit

interface ExitGameUseCase {
    /** Discards the active session and invalidates any unfinished start. Idempotent. */
    operator fun invoke()
}
