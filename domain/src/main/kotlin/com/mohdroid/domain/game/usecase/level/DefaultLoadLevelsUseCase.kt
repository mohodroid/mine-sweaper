package com.mohdroid.domain.game.usecase.level

import com.mohdroid.domain.game.model.level.LevelSummary
import com.mohdroid.domain.game.repository.LevelRepository
import kotlinx.coroutines.CancellationException

internal class DefaultLoadLevelsUseCase(private val repository: LevelRepository) : LoadLevelsUseCase {
    override suspend fun invoke(): Result<List<LevelSummary>> = try {
        val levels = repository.loadLevels().getOrThrow()
        Result.success(levels.sortedBy { it.difficulty.ordinal })
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception)
    }
}
