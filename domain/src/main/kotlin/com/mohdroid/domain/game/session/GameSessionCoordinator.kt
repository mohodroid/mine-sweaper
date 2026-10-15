package com.mohdroid.domain.game.session

import com.mohdroid.domain.game.mapper.EngineGameStateMapper
import com.mohdroid.domain.game.mapper.toEngine
import com.mohdroid.domain.game.model.board.Direction
import com.mohdroid.domain.game.model.board.Position
import com.mohdroid.domain.game.model.board.Reminder
import com.mohdroid.domain.game.model.level.Difficulty
import com.mohdroid.domain.game.model.level.LevelConfiguration
import com.mohdroid.domain.game.model.session.GameState
import com.mohdroid.game.domain.engine.GameEngine
import com.mohdroid.game.domain.model.GameSnapshot
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/** Owns one active engine session; shared by its start, interaction, and exit use cases. */
internal class GameSessionCoordinator(
    private val computationDispatcher: CoroutineDispatcher,
    private val startingLives: Int = 1,
    private val nextSeed: () -> Long = System::nanoTime
) : GameSession {
    init {
        require(startingLives > 0) { "Starting lives must be positive." }
    }

    private val lock = Any()
    private var currentRequest: GameStartRequest? = null
    private var activeGame: ActiveGame? = null

    override fun beginStart(difficulty: Difficulty): GameStartRequest = synchronized(lock) {
        GameStartRequest(difficulty).also { request ->
            currentRequest = request
            activeGame = null
        }
    }

    override suspend fun activate(
        request: GameStartRequest,
        configuration: LevelConfiguration
    ): Result<GameState> = try {
        currentCoroutineContext().ensureActive()
        ensureCurrent(request)
        // Keep generation and initial mapping off the caller's UI dispatcher.
        // Unwrap the engine Result here for compatibility with the project's Kotlin version.
        val prepared = withContext(computationDispatcher) {
            ensureCurrent(request)
            val engine = GameEngine.create(configuration.toEngine(), nextSeed(), startingLives).getOrThrow()
            currentCoroutineContext().ensureActive()
            val mapper = EngineGameStateMapper(request.difficulty)
            PreparedGame(ActiveGame(engine, mapper), mapper.map(engine.snapshot()))
        }
        val callerContext = currentCoroutineContext()
        synchronized(lock) {
            checkCurrent(request)
            callerContext.ensureActive()
            activeGame = prepared.session
            Result.success(prepared.initialState)
        }
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        currentCoroutineContext().ensureActive()
        ensureCurrent(request)
        Result.failure(exception)
    }

    override fun ensureCurrent(request: GameStartRequest) = synchronized(lock) {
        checkCurrent(request)
    }

    override fun movePlayer(direction: Direction): Result<GameState> =
        interact { it.move(direction.toEngine()) }

    override fun setCellReminder(position: Position, reminder: Reminder): Result<GameState> =
        interact { it.setFlag(position.toEngine(), reminder.toEngine()) }

    override fun exit() = synchronized(lock) {
        currentRequest = null
        activeGame = null
    }

    private fun interact(action: (GameEngine) -> GameSnapshot): Result<GameState> = synchronized(lock) {
        val game = activeGame
            ?: return@synchronized Result.failure(IllegalStateException("No active game."))
        try {
            Result.success(game.mapper.map(action(game.engine)))
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    private fun checkCurrent(request: GameStartRequest) {
        if (request !== currentRequest) {
            throw CancellationException("Game start was replaced or exited.")
        }
    }

    private class ActiveGame(val engine: GameEngine, val mapper: EngineGameStateMapper)
    private class PreparedGame(val session: ActiveGame, val initialState: GameState)
}
