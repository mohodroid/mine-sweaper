package com.mohdroid.minesweeper.feature.level

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohdroid.domain.game.usecase.level.LoadLevelsUseCase
import com.mohdroid.minesweeper.feature.level.contract.LevelChange
import com.mohdroid.minesweeper.feature.level.contract.LevelIntent
import com.mohdroid.minesweeper.feature.level.contract.LevelLoadState
import com.mohdroid.minesweeper.feature.level.contract.LevelUiState
import com.mohdroid.minesweeper.feature.level.mapper.toUiModel
import com.mohdroid.minesweeper.ui.error.UiError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Owns the level catalogue and selection, never a gameplay session. */
class LevelViewModel(
    private val loadLevels: LoadLevelsUseCase,
    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main.immediate
) : ViewModel() {
    private val _uiState = MutableStateFlow(LevelUiState())
    val uiState: StateFlow<LevelUiState> = _uiState.asStateFlow()
    private var initialized = false
    private var loadJob: Job? = null
    private var loadGeneration = 0L

    init { onIntent(LevelIntent.Load) }

    fun onIntent(intent: LevelIntent) {
        when (intent) {
            LevelIntent.Load -> if (!initialized) {
                initialized = true
                load()
            }
            LevelIntent.Retry -> if (_uiState.value.loadState == LevelLoadState.ERROR) load()
            is LevelIntent.SelectLevel -> dispatch(LevelChange.LevelSelected(intent.levelId))
            is LevelIntent.NavigationHandled -> dispatch(LevelChange.NavigationHandled(intent.navigation))
            LevelIntent.ScreenResumed -> dispatch(LevelChange.ScreenResumed)
        }
    }

    private fun load() {
        val request = ++loadGeneration
        loadJob?.cancel()
        dispatch(LevelChange.LoadingStarted)
        loadJob = viewModelScope.launch(uiDispatcher) {
            val result = loadLevels()
            if (!isActive || request != loadGeneration) return@launch
            result.fold(
                onSuccess = { levels -> dispatch(LevelChange.LevelsLoaded(levels.map { it.toUiModel() })) },
                onFailure = {
                    dispatch(LevelChange.LoadingFailed(UiError.LEVEL_LOADING_FAILED))
                }
            )
        }
    }

    private fun dispatch(change: LevelChange) {
        _uiState.value = LevelReducer.reduce(_uiState.value, change)
    }
}
