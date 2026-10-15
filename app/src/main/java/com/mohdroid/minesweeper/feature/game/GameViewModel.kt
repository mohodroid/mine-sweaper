package com.mohdroid.minesweeper.feature.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohdroid.domain.game.usecase.exit.ExitGameUseCase
import com.mohdroid.domain.game.usecase.interaction.GameInteraction
import com.mohdroid.domain.game.usecase.start.StartGameUseCase
import com.mohdroid.minesweeper.feature.game.contract.GameChange
import com.mohdroid.minesweeper.feature.game.contract.GameIntent
import com.mohdroid.minesweeper.feature.game.contract.GameUiState
import com.mohdroid.minesweeper.feature.game.mapper.GamePresentationMapper
import com.mohdroid.minesweeper.feature.game.mapper.toDomain
import com.mohdroid.minesweeper.feature.game.model.CellPositionUiModel
import com.mohdroid.minesweeper.feature.game.model.DifficultyUiModel
import com.mohdroid.minesweeper.feature.game.model.MoveDirectionUi
import com.mohdroid.minesweeper.feature.game.model.ReminderUiModel
import com.mohdroid.minesweeper.ui.error.UiError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** One destination-owned game: intents in, immutable UI state out. */
class GameViewModel(
    initialDifficultyId: String,
    private val startGame: StartGameUseCase,
    private val interaction: GameInteraction,
    private val exitGame: ExitGameUseCase,
    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
    private val onDispose: () -> Unit = {}
) : ViewModel() {
    private val initialDifficulty = DifficultyUiModel.values().firstOrNull { it.name == initialDifficultyId }
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    private val presentationMapper = GamePresentationMapper()
    private var initialized = false
    private var loadJob: Job? = null
    private var loadGeneration = 0L

    init { onIntent(GameIntent.Start) }

    fun onIntent(intent: GameIntent) {
        when (intent) {
            GameIntent.Start -> if (!initialized) {
                initialized = true
                val difficulty = initialDifficulty
                if (difficulty == null) dispatch(GameChange.LoadingFailed(UiError.INVALID_LEVEL_SELECTION))
                else start(difficulty)
            }
            is GameIntent.Move -> move(intent.direction)
            is GameIntent.SetReminder -> setReminder(intent.position, intent.reminder)
            is GameIntent.SelectCell -> dispatch(GameChange.CellSelected(intent.position))
            GameIntent.ReviewBoard -> dispatch(GameChange.ReviewRequested)
            GameIntent.Restart, GameIntent.RetryLoad -> _uiState.value.difficulty?.let(::start)
            GameIntent.Exit -> exit()
            is GameIntent.NavigationHandled -> dispatch(GameChange.NavigationHandled(intent.navigation))
        }
    }

    private fun start(difficulty: DifficultyUiModel) {
        if (_uiState.value.isExiting) return
        val request = ++loadGeneration
        loadJob?.cancel()
        presentationMapper.reset()
        dispatch(GameChange.LoadingStarted(difficulty))
        loadJob = viewModelScope.launch(uiDispatcher) {
            val result = startGame(difficulty.toDomain())
            if (!isActive || request != loadGeneration) return@launch
            result.fold(
                onSuccess = { dispatch(GameChange.GameLoaded(presentationMapper.map(it))) },
                onFailure = {
                    dispatch(GameChange.LoadingFailed(UiError.GAME_LOADING_FAILED))
                }
            )
        }
    }

    private fun move(direction: MoveDirectionUi) {
        if (!_uiState.value.canMove) return
        val next = interaction.movePlayer(direction.toDomain()).getOrNull() ?: return
        dispatch(GameChange.GameUpdated(presentationMapper.map(next)))
    }

    private fun setReminder(position: CellPositionUiModel, reminder: ReminderUiModel) {
        if (!_uiState.value.canMove) return
        val next = interaction.setCellReminder(position.toDomain(), reminder.toDomain()).getOrNull() ?: return
        dispatch(GameChange.GameUpdated(presentationMapper.map(next)))
    }

    private fun exit() {
        if (_uiState.value.isExiting) return
        clearSession()
        dispatch(GameChange.Exited)
    }

    private fun clearSession() {
        ++loadGeneration
        loadJob?.cancel()
        loadJob = null
        exitGame()
        presentationMapper.reset()
    }

    private fun dispatch(change: GameChange) {
        _uiState.value = GameReducer.reduce(_uiState.value, change)
    }

    override fun onCleared() {
        try {
            clearSession()
        } finally {
            onDispose()
            super.onCleared()
        }
    }
}
