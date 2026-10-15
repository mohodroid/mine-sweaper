package com.mohdroid.minesweeper.di

import com.mohdroid.domain.game.di.gameScopeQualifier
import com.mohdroid.minesweeper.feature.game.GameViewModel
import com.mohdroid.minesweeper.feature.level.LevelViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import java.util.UUID

val appModule = module {
    viewModel { LevelViewModel(loadLevels = get()) }

    viewModel { parameters ->
        val levelId = parameters.get<String>()
        // The navigation destination owns this ViewModel and its independent session.
        val gameScope = getKoin().createScope(UUID.randomUUID().toString(), gameScopeQualifier)
        try {
            GameViewModel(
                initialDifficultyId = levelId,
                startGame = gameScope.get(),
                interaction = gameScope.get(),
                exitGame = gameScope.get(),
                onDispose = gameScope::close
            )
        } catch (exception: Exception) {
            gameScope.close()
            throw exception
        }
    }
}
