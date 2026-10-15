package com.mohdroid.domain.game

import com.mohdroid.domain.game.model.board.*
import com.mohdroid.domain.game.model.level.*
import com.mohdroid.domain.game.model.session.*
import com.mohdroid.domain.game.repository.LevelRepository
import com.mohdroid.domain.game.session.GameSessionCoordinator
import com.mohdroid.domain.game.usecase.start.DefaultStartGameUseCase
import com.mohdroid.domain.game.usecase.start.StartGameUseCase
import com.mohdroid.domain.game.usecase.interaction.DefaultGameInteraction
import com.mohdroid.domain.game.usecase.interaction.GameInteraction
import com.mohdroid.domain.game.usecase.exit.DefaultExitGameUseCase
import com.mohdroid.domain.game.usecase.exit.ExitGameUseCase
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.coroutines.CoroutineContext

class GameUseCasesTest {
    @Test fun `start owns difficulty and creates a clean hidden board`() = runBlocking {
        val cases = gameCases()
        val game = cases.startGame(Difficulty.MEDIUM).getOrThrow()
        assertEquals(Difficulty.MEDIUM, game.difficulty)
        assertEquals(Position(0, 0), game.player)
        assertEquals(0, game.moves)
        assertEquals(0, game.foundKeys)
        assertEquals(1, game.remainingLives)
        assertNull(game.board[1][0].tile)
        assertFalse(game.exitUnlocked)
    }

    @Test fun `movement and reminders use the same active engine`() = runBlocking {
        val cases = gameCases()
        cases.startGame(Difficulty.EASY).getOrThrow()
        val flagged = cases.interaction.setCellReminder(Position(1, 0), Reminder.KEY).getOrThrow()
        assertEquals(Reminder.KEY, flagged.board[1][0].reminder)
        assertEquals(0, flagged.moves)
        val moved = cases.interaction.movePlayer(Direction.SOUTH).getOrThrow()
        assertEquals(1, moved.moves)
        assertEquals(1, moved.foundKeys)
        assertEquals(Reminder.NONE, moved.board[1][0].reminder)
        assertTrue(moved.exitUnlocked)
    }

    @Test fun `restart resets counters flags lives and player using a new generation seed`() = runBlocking {
        var seed = 40L
        var loads = 0
        val cases = createGameCases(repository {
            loads++
            Result.success(clueLevel())
        }, Dispatchers.Unconfined, nextSeed = { seed++ })
        cases.startGame(Difficulty.HARD).getOrThrow()
        cases.interaction.setCellReminder(Position(1, 1), Reminder.KEY)
        assertEquals(GamePhase.LOST, cases.interaction.movePlayer(Direction.EAST).getOrThrow().phase)
        val restart = cases.startGame(Difficulty.HARD).getOrThrow()
        assertEquals(2, loads)
        assertEquals(42L, seed)
        assertEquals(GamePhase.PLAYING, restart.phase)
        assertEquals(0, restart.moves)
        assertEquals(0, restart.foundKeys)
        assertEquals(1, restart.remainingLives)
        assertEquals(Position(0, 0), restart.player)
        assertTrue(restart.board.flatten().all { it.reminder == Reminder.NONE })
        assertNull(restart.board[0][1].tile)
    }

    @Test fun `failed loading can be retried through the same start contract`() = runBlocking {
        var fail = true
        val cases = gameCases(repository {
            if (fail) Result.failure(IllegalStateException("Missing level")) else Result.success(simpleLevel())
        })
        assertEquals("Missing level", cases.startGame(Difficulty.HARD).exceptionOrNull()?.message)
        assertTrue(cases.interaction.movePlayer(Direction.SOUTH).isFailure)
        fail = false
        assertEquals(Difficulty.HARD, cases.startGame(Difficulty.HARD).getOrThrow().difficulty)
        assertEquals(1, cases.interaction.movePlayer(Direction.SOUTH).getOrThrow().foundKeys)
    }

    @Test fun `invalid configuration fails in the domain instead of leaking an engine`() = runBlocking {
        val cases = gameCases(repository { Result.success(simpleLevel().copy(rows = 0)) })
        assertTrue(cases.startGame(Difficulty.EASY).isFailure)
        assertTrue(cases.interaction.movePlayer(Direction.SOUTH).isFailure)
    }

    @Test fun `a failed restart discards the old game and propagates thrown repository errors`() = runBlocking {
        var fail = false
        val cases = gameCases(repository {
            if (fail) throw IllegalStateException("Broken source")
            Result.success(simpleLevel())
        })
        cases.startGame(Difficulty.EASY).getOrThrow()
        fail = true
        assertEquals("Broken source", cases.startGame(Difficulty.MEDIUM).exceptionOrNull()?.message)
        assertTrue(cases.interaction.setCellReminder(Position(1, 0), Reminder.KEY).isFailure)
    }

    @Test fun `exit is idempotent and a subsequent start gets an independent session`() = runBlocking {
        val cases = gameCases()
        cases.startGame(Difficulty.EASY).getOrThrow()
        cases.interaction.movePlayer(Direction.SOUTH)
        cases.exitGame()
        cases.exitGame()
        assertTrue(cases.interaction.movePlayer(Direction.NORTH).isFailure)
        assertTrue(cases.interaction.setCellReminder(Position(1, 0), Reminder.MINE).isFailure)
        assertEquals(0, cases.startGame(Difficulty.HARD).getOrThrow().foundKeys)
    }

    @Test fun `separately created use-case scopes never share an active game`() = runBlocking {
        val first = gameCases()
        val second = gameCases()
        first.startGame(Difficulty.EASY).getOrThrow()
        second.startGame(Difficulty.HARD).getOrThrow()
        first.interaction.movePlayer(Direction.SOUTH)
        first.exitGame()
        assertEquals(0, second.interaction.movePlayer(Direction.EAST).getOrThrow().foundKeys)
    }

    @Test fun `no-op actions retain identity and changes share unrelated rows and cells`() = runBlocking {
        val cases = gameCases()
        val initial = cases.startGame(Difficulty.EASY).getOrThrow()
        assertSame(initial, cases.interaction.movePlayer(Direction.NORTH).getOrThrow())
        val moved = cases.interaction.movePlayer(Direction.EAST).getOrThrow()
        assertSame(initial.board[1], moved.board[1])
        assertSame(initial.board[0][0], moved.board[0][0])
        assertNotSame(initial.board[0], moved.board[0])
        val returned = cases.interaction.movePlayer(Direction.WEST).getOrThrow()
        assertSame(moved.board, returned.board)
        assertNull(initial.board[0][1].tile)
        assertEquals(TileKind.STEP, moved.board[0][1].tile)
    }

    @Test fun `reminder set change toggle and clear preserve hidden content`() = runBlocking {
        val cases = gameCases()
        cases.startGame(Difficulty.EASY).getOrThrow()
        fun flag(reminder: Reminder) = cases.interaction.setCellReminder(Position(1, 0), reminder).getOrThrow()
        assertEquals(Reminder.MINE, flag(Reminder.MINE).board[1][0].reminder)
        assertEquals(Reminder.KEY, flag(Reminder.KEY).board[1][0].reminder)
        assertEquals(Reminder.NONE, flag(Reminder.KEY).board[1][0].reminder)
        flag(Reminder.MINE)
        val cleared = flag(Reminder.NONE)
        assertNull(cleared.board[1][0].tile)
        assertEquals(0, cleared.moves)
        assertEquals(Reminder.NONE, cleared.board[1][0].reminder)
    }

    @Test fun `mine loss reveals review board and terminal actions are no-ops`() = runBlocking {
        val cases = gameCases(repository { Result.success(clueLevel()) })
        cases.startGame(Difficulty.EASY).getOrThrow()
        val lost = cases.interaction.movePlayer(Direction.EAST).getOrThrow()
        assertEquals(GamePhase.LOST, lost.phase)
        assertEquals(0, lost.remainingLives)
        assertEquals(Position(0, 0), lost.player)
        assertTrue(lost.board.flatten().all { it.tile != null })
        assertSame(lost, cases.interaction.movePlayer(Direction.SOUTH).getOrThrow())
        assertSame(lost, cases.interaction.setCellReminder(Position(1, 1), Reminder.KEY).getOrThrow())
    }

    @Test fun `configured multiple lives are preserved without changing the default`() = runBlocking {
        val cases = createGameCases(repository { Result.success(clueLevel()) }, Dispatchers.Unconfined, 2)
        cases.startGame(Difficulty.EASY).getOrThrow()
        val hit = cases.interaction.movePlayer(Direction.EAST).getOrThrow()
        assertEquals(GamePhase.PLAYING, hit.phase)
        assertEquals(2, hit.startingLives)
        assertEquals(1, hit.remainingLives)
        assertNull(hit.board[1][1].tile)
        assertSame(hit, cases.interaction.movePlayer(Direction.EAST).getOrThrow())
    }

    @Test fun `keys unlock exit and victory is mapped to the domain`() = runBlocking {
        val cases = gameCases()
        val initial = cases.startGame(Difficulty.EASY).getOrThrow()
        cases.interaction.movePlayer(Direction.EAST)
        assertEquals(1, cases.interaction.movePlayer(Direction.EAST).getOrThrow().moves)
        cases.interaction.movePlayer(Direction.WEST)
        cases.interaction.movePlayer(Direction.SOUTH)
        cases.interaction.movePlayer(Direction.NORTH)
        cases.interaction.movePlayer(Direction.EAST)
        val won = cases.interaction.movePlayer(Direction.EAST).getOrThrow()
        assertFalse(initial.exitUnlocked)
        assertTrue(won.exitUnlocked)
        assertEquals(GamePhase.WON, won.phase)
        assertTrue(won.board.flatten().all { it.tile != null })
    }

    @Test fun `older successful starts cannot replace a newer game`() = runBlocking {
        val release = CompletableDeferred<Unit>()
        val entered = CompletableDeferred<Unit>()
        val cases = gameCases(repository { difficulty ->
            if (difficulty == Difficulty.EASY) {
                entered.complete(Unit)
                release.await()
            }
            Result.success(simpleLevel())
        })
        val old = async { cases.startGame(Difficulty.EASY) }
        entered.await()
        cases.startGame(Difficulty.HARD).getOrThrow()
        release.complete(Unit)
        old.join()
        assertTrue(old.isCancelled)
        assertEquals(Difficulty.HARD, cases.interaction.movePlayer(Direction.SOUTH).getOrThrow().difficulty)
    }

    @Test fun `older failed loads do not overwrite a newer session`() = runBlocking {
        val entered = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val cases = gameCases(repository { difficulty ->
            if (difficulty == Difficulty.EASY) {
                entered.complete(Unit)
                release.await()
                Result.failure(IllegalStateException("Old failure"))
            } else Result.success(simpleLevel())
        })
        val old = async { cases.startGame(Difficulty.EASY) }
        entered.await()
        cases.startGame(Difficulty.HARD).getOrThrow()
        release.complete(Unit)
        old.join()
        assertTrue(old.isCancelled)
        assertEquals(Difficulty.HARD, cases.interaction.movePlayer(Direction.SOUTH).getOrThrow().difficulty)
    }

    @Test fun `exit invalidates an in-flight start even without cancelling its caller`() = runBlocking {
        val entered = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val cases = gameCases(repository {
            entered.complete(Unit)
            release.await()
            Result.success(simpleLevel())
        })
        val pending = async { cases.startGame(Difficulty.EASY) }
        entered.await()
        cases.exitGame()
        release.complete(Unit)
        pending.join()
        assertTrue(pending.isCancelled)
        assertTrue(cases.interaction.movePlayer(Direction.SOUTH).isFailure)
    }

    @Test fun `cancelled callers cannot activate a game after non-cooperative loading`() = runBlocking {
        val entered = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val cases = gameCases(repository {
            withContext(NonCancellable) {
                entered.complete(Unit)
                release.await()
                Result.success(simpleLevel())
            }
        })
        val pending = async { cases.startGame(Difficulty.EASY) }
        entered.await()
        pending.cancel()
        release.complete(Unit)
        pending.join()
        assertTrue(pending.isCancelled)
        assertTrue(cases.interaction.movePlayer(Direction.SOUTH).isFailure)
    }

    @Test fun `cancellation exceptions are not converted to loading errors`() = runBlocking {
        val cases = gameCases(repository { throw CancellationException("Cancelled source") })
        val pending = async { cases.startGame(Difficulty.EASY) }
        pending.join()
        assertTrue(pending.isCancelled)
    }

    private fun gameCases(repo: LevelRepository = repository { Result.success(simpleLevel()) }) =
        createGameCases(repo, Dispatchers.Unconfined, nextSeed = { 7L })

    @Test fun `exit during queued generation cannot reactivate a session`() = runBlocking {
        val dispatcher = QueuedDispatcher()
        val cases = createGameCases(repository { Result.success(simpleLevel()) }, dispatcher)
        val pending = async(start = CoroutineStart.UNDISPATCHED) { cases.startGame(Difficulty.EASY) }
        assertEquals(1, dispatcher.tasks.size)
        cases.exitGame()
        dispatcher.runNext()
        pending.join()
        assertTrue(pending.isCancelled)
        assertTrue(cases.interaction.movePlayer(Direction.SOUTH).isFailure)
    }

    @Test fun `a newer start wins even when older generation finishes first`() = runBlocking {
        val dispatcher = QueuedDispatcher()
        val cases = createGameCases(repository { Result.success(simpleLevel()) }, dispatcher)
        val old = async(start = CoroutineStart.UNDISPATCHED) { cases.startGame(Difficulty.EASY) }
        val current = async(start = CoroutineStart.UNDISPATCHED) { cases.startGame(Difficulty.HARD) }
        assertEquals(2, dispatcher.tasks.size)
        dispatcher.runNext()
        old.join()
        assertTrue(old.isCancelled)
        assertTrue(cases.interaction.movePlayer(Direction.SOUTH).isFailure)
        dispatcher.runNext()
        assertEquals(Difficulty.HARD, current.await().getOrThrow().difficulty)
        assertEquals(Difficulty.HARD, cases.interaction.movePlayer(Direction.SOUTH).getOrThrow().difficulty)
    }

    @Test fun `cancelling queued generation does not activate a session`() = runBlocking {
        val dispatcher = QueuedDispatcher()
        val cases = createGameCases(repository { Result.success(simpleLevel()) }, dispatcher)
        val pending = async(start = CoroutineStart.UNDISPATCHED) { cases.startGame(Difficulty.EASY) }
        pending.cancel()
        dispatcher.runNext()
        pending.join()
        assertTrue(pending.isCancelled)
        assertTrue(cases.interaction.movePlayer(Direction.SOUTH).isFailure)
    }

    private class QueuedDispatcher : CoroutineDispatcher() {
        val tasks = java.util.ArrayDeque<Runnable>()
        override fun dispatch(context: CoroutineContext, block: Runnable) { tasks.addLast(block) }
        fun runNext() { tasks.removeFirst().run() }
    }

    private fun repository(load: suspend (Difficulty) -> Result<LevelConfiguration>) = object : LevelRepository {
        override suspend fun loadLevel(difficulty: Difficulty): Result<LevelConfiguration> = load(difficulty)
        override suspend fun loadLevels(): Result<List<LevelSummary>> = error("Unexpected catalogue load")
    }

    private fun simpleLevel() = LevelConfiguration(2, 3, listOf(
        LevelAsset(TileKind.START, 1, listOf(Position(0, 0))),
        LevelAsset(TileKind.EXIT, 1, listOf(Position(0, 2))),
        LevelAsset(TileKind.KEY, 1, listOf(Position(1, 0)))
    ))

    private fun clueLevel() = LevelConfiguration(3, 3, listOf(
        LevelAsset(TileKind.START, 1, listOf(Position(0, 0))),
        LevelAsset(TileKind.EXIT, 1, listOf(Position(2, 2))),
        LevelAsset(TileKind.MINE, 1, listOf(Position(0, 1))),
        LevelAsset(TileKind.KEY, 1, listOf(Position(1, 1)))
    ))

    // Test-only assembly: production wiring is owned by Koin, not a public factory.
    private data class TestGameCases(
        val startGame: StartGameUseCase,
        val interaction: GameInteraction,
        val exitGame: ExitGameUseCase
    )

    private fun createGameCases(
        repository: LevelRepository,
        dispatcher: CoroutineDispatcher,
        startingLives: Int = 1,
        nextSeed: () -> Long = { 7L }
    ): TestGameCases {
        val session = GameSessionCoordinator(dispatcher, startingLives, nextSeed)
        return TestGameCases(
            DefaultStartGameUseCase(repository, session),
            DefaultGameInteraction(session),
            DefaultExitGameUseCase(session)
        )
    }
}
