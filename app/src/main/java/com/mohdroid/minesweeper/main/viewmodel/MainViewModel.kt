package com.mohdroid.minesweeper.main.viewmodel

import androidx.lifecycle.viewModelScope
import com.mohdroid.algorithm.DirectionalOfMovement
import com.mohdroid.algorithm.MineSweeper
import com.mohdroid.algorithm.MissionCoordinator
import com.mohdroid.algorithm.MissionCoordinator.PlayerState.*
import com.mohdroid.minesweeper.framework.*
import com.mohdroid.minesweeper.main.DifficultyLevel
import com.mohdroid.minesweeper.main.repository.GameRepository
import com.mohdroid.minesweeper.main.toList
import com.mohdroid.minesweeper.main.viewmodel.MainViewModel.State
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


data class FinishGame(
    val isFinished: Boolean = false,
    val isWin: Boolean = false
)

class MainViewModel(
    private val gameRepository: GameRepository
) : DiscountopiaStatefulViewModel<State>(State()) {

    data class State(
        val boardGame: LoadableData<List<List<MineSweeper.Node>>> = NotLoaded,
        val columns: Int = 0,
        val rows: Int = 0,
        val gameState: MissionCoordinator.PlayerState = CONTINUE,
        val numberOfMovement: Int = 0,
        val numberOfFoundKeys: Int = 0,
        val totalNumberOfKeys: Int = 0,
        val numberOfMines: Int = 0
    )

    sealed class Event {
        data class ShowFinishGameDialog(val finishGame: FinishGame = FinishGame()) : Event()
    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = eventChannel.receiveAsFlow()

    private lateinit var mc: MissionCoordinator
    private lateinit var level: DifficultyLevel

    fun onLevel(level: DifficultyLevel) {
        this.level = level
        applyState { copy(boardGame = Loading) }
        viewModelScope.launch {
            val gameLevel = gameRepository.getGameLevel(level)
            mc = MissionCoordinator(gameLevel)
            startGame(mc)
        }
    }

    private fun startGame(mc: MissionCoordinator) {
        applyState {
            copy(
                boardGame = Loaded(mc.mineSweeper.gameBoard.toList()),
                columns = mc.mineSweeper.col,
                rows = mc.mineSweeper.row,
                totalNumberOfKeys = mc.totalNumberOfKeys,
                numberOfFoundKeys = mc.numberOfFoundKeys,
                gameState = CONTINUE,
                numberOfMovement = 0,
                numberOfMines = mc.numberOfMines
            )
        }
    }

    fun onMovement(directionalOfMovement: DirectionalOfMovement) {
        val playerState = mc.play(directionalOfMovement)
        val newNumber =
            if (playerState != null) currentState.numberOfMovement.inc()
            else currentState.numberOfMovement
        val isFinished = playerState == LOOSE || playerState == WIN
        applyState {
            copy(
                gameState = playerState,
                boardGame = Loaded(mc.mineSweeper.gameBoard.toList()),
                numberOfMovement = newNumber,
                totalNumberOfKeys = mc.totalNumberOfKeys,
                numberOfFoundKeys = mc.numberOfFoundKeys
            )
        }
        viewModelScope.launch {
            eventChannel.send(
                Event.ShowFinishGameDialog(
                    FinishGame(
                        isFinished,
                        playerState == WIN
                    )
                )
            )
        }
    }

    fun onTryAgain() {
        viewModelScope.launch {
            eventChannel.send(
                Event.ShowFinishGameDialog()
            )
        }
        onLevel(level)
    }


}