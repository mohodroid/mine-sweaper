package com.mohdroid.domain.game.usecase.start

import com.mohdroid.domain.game.model.level.Difficulty
import com.mohdroid.domain.game.model.session.GameState
import com.mohdroid.domain.game.repository.LevelRepository
import com.mohdroid.domain.game.session.GameSession
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** Coordinates level loading and starting; engine details belong to the session implementation. */
internal class DefaultStartGameUseCase(
    private val repository: LevelRepository,
    private val session: GameSession
) : StartGameUseCase {
    override suspend fun invoke(difficulty: Difficulty): Result<GameState> {
        currentCoroutineContext().ensureActive()
        val request = session.beginStart(difficulty)
        return try {
            val configuration = repository.loadLevel(difficulty).getOrThrow()
            currentCoroutineContext().ensureActive()
            session.activate(request, configuration)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            currentCoroutineContext().ensureActive()
            // A stale repository failure must not become an error for the newer game.
            session.ensureCurrent(request)
            Result.failure(exception)
        }
    }
}
