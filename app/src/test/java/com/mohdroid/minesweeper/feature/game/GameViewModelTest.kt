package com.mohdroid.minesweeper.feature.game

import com.mohdroid.domain.game.model.board.*
import com.mohdroid.domain.game.model.level.Difficulty
import com.mohdroid.domain.game.model.session.*
import com.mohdroid.domain.game.usecase.start.StartGameUseCase
import com.mohdroid.domain.game.usecase.exit.ExitGameUseCase
import com.mohdroid.domain.game.usecase.interaction.GameInteraction
import com.mohdroid.minesweeper.feature.TestViewModelOwner
import com.mohdroid.minesweeper.feature.game.contract.*
import com.mohdroid.minesweeper.feature.game.model.*
import com.mohdroid.minesweeper.ui.error.UiError
import kotlinx.coroutines.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Test

class GameViewModelTest {
    private val owner = TestViewModelOwner()
    @After fun clearViewModels() = owner.clear()

    @Test fun `initial selection starts exactly once`() {
        val difficulties = mutableListOf<Difficulty>()
        val vm = viewModel("MEDIUM", load = {
            difficulties.add(it)
            Result.success(gameState(it))
        })
        vm.onIntent(GameIntent.Start)
        assertEquals(listOf(Difficulty.MEDIUM), difficulties)
        assertEquals(GameLoadState.READY, vm.uiState.value.loadState)
        assertEquals(DifficultyUiModel.MEDIUM, vm.uiState.value.difficulty)
    }

    @Test fun `load failure has a safe retryable error and retry keeps difficulty`() {
        var fail = true
        val difficulties = mutableListOf<Difficulty>()
        val vm = viewModel("HARD", load = {
            difficulties.add(it)
            if (fail) Result.failure(IllegalStateException("Sensitive asset path"))
            else Result.success(gameState(it))
        })
        assertEquals(GameLoadState.ERROR, vm.uiState.value.loadState)
        assertEquals(UiError.GAME_LOADING_FAILED, vm.uiState.value.error)
        assertTrue(vm.uiState.value.error!!.canRetry)
        assertNull(vm.uiState.value.game)
        fail = false
        vm.onIntent(GameIntent.RetryLoad)
        assertEquals(listOf(Difficulty.HARD, Difficulty.HARD), difficulties)
        assertEquals(GameLoadState.READY, vm.uiState.value.loadState)
        assertNull(vm.uiState.value.error)
    }

    @Test fun `invalid identifier offers back navigation instead of a broken retry`() {
        var loads = 0
        val vm = viewModel("UNKNOWN", load = { loads++; Result.success(gameState(it)) })
        assertEquals(UiError.INVALID_LEVEL_SELECTION, vm.uiState.value.error)
        assertFalse(vm.uiState.value.error!!.canRetry)
        vm.onIntent(GameIntent.RetryLoad)
        assertEquals(0, loads)
        vm.onIntent(GameIntent.Exit)
        assertEquals(GameNavigation.BackToLevels, vm.uiState.value.pendingNavigation)
    }

    @Test fun `movement and reminders are mapped to domain contracts`() {
        val interaction = StubInteraction(Result.success(gameState().copy(
            player = Position(1, 0), moves = 1, foundKeys = 1
        )))
        val vm = viewModel(interaction = interaction)
        vm.onIntent(GameIntent.Move(MoveDirectionUi.DOWN))
        assertEquals(listOf(Direction.SOUTH), interaction.moves)
        assertEquals(CellPositionUiModel(1, 0), vm.uiState.value.game!!.player)
        assertEquals(1, vm.uiState.value.game!!.stats.foundKeys)
        vm.onIntent(GameIntent.SetReminder(CellPositionUiModel(1, 1), ReminderUiModel.KEY))
        assertEquals(listOf(Position(1, 1) to Reminder.KEY), interaction.reminders)
    }

    @Test fun `no-op actions retain published UI state identity`() {
        val snapshot = gameState()
        val interaction = StubInteraction(Result.success(snapshot))
        val vm = viewModel(load = { Result.success(snapshot) }, interaction = interaction)
        val initial = vm.uiState.value
        vm.onIntent(GameIntent.Move(MoveDirectionUi.UP))
        vm.onIntent(GameIntent.SetReminder(CellPositionUiModel(0, 0), ReminderUiModel.MINE))
        assertSame(initial, vm.uiState.value)
    }

    @Test fun `existing interaction failure policy remains unchanged`() {
        val interaction = StubInteraction(Result.failure(IllegalStateException("Interaction failed")))
        val vm = viewModel(interaction = interaction)
        val initial = vm.uiState.value
        vm.onIntent(GameIntent.Move(MoveDirectionUi.DOWN))
        vm.onIntent(GameIntent.SetReminder(CellPositionUiModel(1, 0), ReminderUiModel.KEY))
        assertSame(initial, vm.uiState.value)
        assertNull(vm.uiState.value.error)
    }

    @Test fun `terminal state blocks gameplay and allows board review`() {
        val interaction = StubInteraction(Result.success(gameState()))
        val vm = viewModel(
            load = { Result.success(gameState(it).copy(phase = GamePhase.LOST, remainingLives = 0)) },
            interaction = interaction
        )
        vm.onIntent(GameIntent.Move(MoveDirectionUi.DOWN))
        vm.onIntent(GameIntent.SetReminder(CellPositionUiModel(1, 0), ReminderUiModel.KEY))
        assertTrue(interaction.moves.isEmpty())
        assertTrue(interaction.reminders.isEmpty())
        assertTrue(vm.uiState.value.isFinished)
        vm.onIntent(GameIntent.ReviewBoard)
        assertTrue(vm.uiState.value.isReviewingBoard)
    }

    @Test fun `restart clears selection and resets the game through the start contract`() {
        var starts = 0
        val vm = viewModel(load = { starts++; Result.success(gameState(it)) })
        vm.onIntent(GameIntent.SelectCell(CellPositionUiModel(1, 0)))
        assertNotNull(vm.uiState.value.selectedCell)
        vm.onIntent(GameIntent.Restart)
        assertEquals(2, starts)
        assertNull(vm.uiState.value.selectedCell)
        assertFalse(vm.uiState.value.isReviewingBoard)
        assertEquals(0, vm.uiState.value.game!!.stats.moves)
    }

    @Test fun `a superseded load cannot publish over a newer restart`() = runBlocking {
        val release = CompletableDeferred<Unit>()
        var starts = 0
        val vm = viewModel(load = {
            val generation = ++starts
            if (generation == 1) withContext(NonCancellable) { release.await() }
            Result.success(gameState(it).copy(moves = generation))
        })
        assertEquals(GameLoadState.LOADING, vm.uiState.value.loadState)
        vm.onIntent(GameIntent.Restart)
        val current = vm.uiState.value
        assertEquals(GameLoadState.READY, current.loadState)
        assertEquals(2, current.game!!.stats.moves)
        release.complete(Unit)
        yield()
        assertSame(current, vm.uiState.value)
    }

    @Test fun `exit cancels loading and retains one navigation request until acknowledged`() = runBlocking {
        val release = CompletableDeferred<Unit>()
        var exits = 0
        val vm = viewModel(
            load = {
                withContext(NonCancellable) { release.await() }
                Result.success(gameState(it))
            },
            onExit = { exits++ }
        )
        vm.onIntent(GameIntent.Exit)
        val exited = vm.uiState.value
        vm.onIntent(GameIntent.Exit)
        assertSame(exited, vm.uiState.value)
        assertEquals(1, exits)
        assertTrue(exited.isExiting)
        assertEquals(GameNavigation.BackToLevels, exited.pendingNavigation)
        release.complete(Unit)
        yield()
        assertSame(exited, vm.uiState.value)
        vm.onIntent(GameIntent.NavigationHandled(GameNavigation.BackToLevels))
        assertNull(vm.uiState.value.pendingNavigation)
        vm.onIntent(GameIntent.Exit)
        vm.onIntent(GameIntent.Restart)
        vm.onIntent(GameIntent.Move(MoveDirectionUi.DOWN))
        assertEquals(1, exits)
        assertNull(vm.uiState.value.pendingNavigation)
        assertNull(vm.uiState.value.game)
    }

    @Test fun `clearing the ViewModel releases its domain session and Koin scope`() {
        var exits = 0
        var disposals = 0
        viewModel(onExit = { exits++ }, onDispose = { disposals++ })
        owner.clear()
        assertEquals(1, exits)
        assertEquals(1, disposals)
    }

    private fun viewModel(
        difficultyId: String = "EASY",
        load: suspend (Difficulty) -> Result<GameState> = { Result.success(gameState(it)) },
        interaction: GameInteraction = StubInteraction(Result.success(gameState())),
        onExit: () -> Unit = {},
        onDispose: () -> Unit = {}
    ) = owner.keep(GameViewModel(
        initialDifficultyId = difficultyId,
        startGame = object : StartGameUseCase {
            override suspend fun invoke(difficulty: Difficulty) = load(difficulty)
        },
        interaction = interaction,
        exitGame = object : ExitGameUseCase { override fun invoke() = onExit() },
        uiDispatcher = Dispatchers.Unconfined,
        onDispose = onDispose
    ))
}
