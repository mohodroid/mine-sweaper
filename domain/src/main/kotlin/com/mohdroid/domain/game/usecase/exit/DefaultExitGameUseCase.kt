package com.mohdroid.domain.game.usecase.exit

import com.mohdroid.domain.game.session.GameSession

internal class DefaultExitGameUseCase(private val session: GameSession) : ExitGameUseCase {
    override fun invoke() = session.exit()
}
