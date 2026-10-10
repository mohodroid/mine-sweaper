package com.mohdroid.minesweeper.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohdroid.game.domain.DifficultyLevel
import com.mohdroid.game.domain.Direction
import com.mohdroid.game.domain.GamePhase
import com.mohdroid.game.domain.GameSession
import com.mohdroid.game.domain.GameSnapshot
import com.mohdroid.game.domain.FlagKind
import com.mohdroid.game.domain.Position
import com.mohdroid.minesweeper.main.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CancellationException

enum class GameLoadState { IDLE, LOADING, READY, ERROR }

data class GameUiState(
    val loadState: GameLoadState = GameLoadState.IDLE,
    val difficulty: DifficultyLevel? = null,
    val game: GameSnapshot? = null,
    val errorMessage: String? = null
) {
    val canMove: Boolean get() = loadState == GameLoadState.READY && game?.phase == GamePhase.PLAYING
    val isFinished: Boolean get() = game?.phase == GamePhase.WON || game?.phase == GamePhase.LOST
}

class MainViewModel(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var session: GameSession? = null
    private var loadJob: Job? = null

    fun onLevel(level: DifficultyLevel) {
        loadJob?.cancel()
        session = null
        _uiState.value = GameUiState(loadState = GameLoadState.LOADING, difficulty = level)
        loadJob = viewModelScope.launch {
            val levelResult = gameRepository.loadLevel(level)
            if (!isActive) return@launch
            if (levelResult.isFailure) {
                showError(levelResult.exceptionOrNull() ?: IllegalStateException("Unable to load level."))
                return@launch
            }

            val createdSession = try {
                withContext(Dispatchers.Default) {
                    GameSession.create(levelResult.getOrThrow()).getOrThrow()
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                showError(exception)
                return@launch
            }
            if (!isActive) return@launch
            session = createdSession
            _uiState.value = GameUiState(
                loadState = GameLoadState.READY,
                difficulty = level,
                game = createdSession.snapshot()
            )
        }
    }

    fun onMovement(direction: Direction) {
        val activeSession = session ?: return
        if (!_uiState.value.canMove) return
        val next = activeSession.move(direction)
        if (next === _uiState.value.game) return
        _uiState.value = _uiState.value.copy(game = next)
    }

    fun onFlag(position: Position, flag: FlagKind) {
        val activeSession = session ?: return
        if (!_uiState.value.canMove) return
        val next = activeSession.setFlag(position, flag)
        if (next === _uiState.value.game) return
        _uiState.value = _uiState.value.copy(game = next)
    }

    fun onTryAgain() {
        _uiState.value.difficulty?.let(::onLevel)
    }

    fun onRetryLoad() {
        _uiState.value.difficulty?.let(::onLevel)
    }

    private fun showError(throwable: Throwable) {
        _uiState.value = _uiState.value.copy(
            loadState = GameLoadState.ERROR,
            game = null,
            errorMessage = throwable.message ?: "Unable to load this level."
        )
    }
}
