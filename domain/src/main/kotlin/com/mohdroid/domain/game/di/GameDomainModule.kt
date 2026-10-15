package com.mohdroid.domain.game.di

import com.mohdroid.domain.game.session.GameSession
import com.mohdroid.domain.game.session.GameSessionCoordinator
import com.mohdroid.domain.game.usecase.exit.DefaultExitGameUseCase
import com.mohdroid.domain.game.usecase.exit.ExitGameUseCase
import com.mohdroid.domain.game.usecase.interaction.DefaultGameInteraction
import com.mohdroid.domain.game.usecase.interaction.GameInteraction
import com.mohdroid.domain.game.usecase.start.DefaultStartGameUseCase
import com.mohdroid.domain.game.usecase.start.StartGameUseCase
import com.mohdroid.domain.game.usecase.level.DefaultLoadLevelsUseCase
import com.mohdroid.domain.game.usecase.level.LoadLevelsUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.onClose

/** Each game-owning ViewModel creates a separate scope with this qualifier. */
val gameScopeQualifier = named("game.session")
private val computationDispatcherQualifier = named("game.computationDispatcher")

val gameDomainModule = module {
    // Level selection does not create or share a gameplay session.
    factory<LoadLevelsUseCase> { DefaultLoadLevelsUseCase(get()) }
    single<CoroutineDispatcher>(computationDispatcherQualifier) { Dispatchers.Default }

    scope(gameScopeQualifier) {
        scoped<GameSession> {
            GameSessionCoordinator(computationDispatcher = get(computationDispatcherQualifier))
        } onClose { session -> session?.exit() }

        scoped<StartGameUseCase> { DefaultStartGameUseCase(get(), get()) }
        scoped<GameInteraction> { DefaultGameInteraction(get()) }
        scoped<ExitGameUseCase> { DefaultExitGameUseCase(get()) }
    }
}
