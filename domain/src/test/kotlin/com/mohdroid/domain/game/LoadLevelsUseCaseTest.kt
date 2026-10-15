package com.mohdroid.domain.game

import com.mohdroid.domain.game.model.board.*
import com.mohdroid.domain.game.model.level.*
import com.mohdroid.domain.game.model.session.*
import com.mohdroid.domain.game.repository.LevelRepository
import com.mohdroid.domain.game.usecase.level.DefaultLoadLevelsUseCase
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test

class LoadLevelsUseCaseTest {
    @Test fun `catalogue is returned in difficulty order without loading a game`() = runBlocking {
        val levels = listOf(summary(Difficulty.HARD), summary(Difficulty.EASY), summary(Difficulty.MEDIUM))
        val result = DefaultLoadLevelsUseCase(repository { Result.success(levels) })().getOrThrow()
        assertEquals(Difficulty.values().toList(), result.map { it.difficulty })
        assertEquals(3, result.size)
    }

    @Test fun `empty catalogue remains a successful empty result`() = runBlocking {
        assertTrue(DefaultLoadLevelsUseCase(repository { Result.success(emptyList()) })().getOrThrow().isEmpty())
    }

    @Test fun `returned and thrown repository failures are preserved`() = runBlocking {
        val failure = IllegalStateException("Missing assets")
        assertSame(failure, DefaultLoadLevelsUseCase(repository { Result.failure(failure) })().exceptionOrNull())
        assertSame(failure, DefaultLoadLevelsUseCase(repository { throw failure })().exceptionOrNull())
    }

    @Test fun `cancellation is not converted to a catalogue error`() = runBlocking {
        val useCase = DefaultLoadLevelsUseCase(repository { throw CancellationException("Cancelled") })
        val task = async { useCase() }
        task.join()
        assertTrue(task.isCancelled)
    }

    private fun summary(difficulty: Difficulty) = LevelSummary(difficulty, 2, 3, 0, 1)

    private fun repository(load: suspend () -> Result<List<LevelSummary>>) = object : LevelRepository {
        override suspend fun loadLevels() = load()
        override suspend fun loadLevel(difficulty: Difficulty): Result<LevelConfiguration> =
            error("Catalogue must not start or load an individual game")
    }
}
