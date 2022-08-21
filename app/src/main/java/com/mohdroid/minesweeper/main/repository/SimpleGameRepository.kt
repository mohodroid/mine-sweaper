package com.mohdroid.minesweeper.main.repository

import com.google.gson.Gson
import com.mohdroid.algorithm.dto.LevelDetailResponse
import com.mohdroid.minesweeper.ConfigManager
import com.mohdroid.minesweeper.framework.CoroutineDispatcherProvider
import com.mohdroid.minesweeper.framework.CoroutineDispatcherProviderImpl
import com.mohdroid.minesweeper.main.DifficultyLevel
import kotlinx.coroutines.withContext

class SimpleGameRepository(
    private val configManager: ConfigManager,
    private val dispatcherProvider: CoroutineDispatcherProvider = CoroutineDispatcherProviderImpl()
) : GameRepository {

    companion object {
        const val easyPath =
            "easyLevelDetail.json"
        const val mediumPath =
            "mediumLevelDetail.json"
        const val hardPath =
            "hardLevelDetail.json"
    }

    private fun getPath(difficultyLevel: DifficultyLevel): String {
        return when (difficultyLevel) {
            DifficultyLevel.EASY -> easyPath
            DifficultyLevel.MEDIUM -> mediumPath
            DifficultyLevel.HARD -> hardPath
        }
    }

    override suspend fun getGameLevel(difficultyLevel: DifficultyLevel) =
        getBoard(difficultyLevel)


    private suspend fun getBoard(difficultyLevel: DifficultyLevel) =
        withContext(dispatcherProvider.ioDispatcher()) {
            configManager.getJson(getPath(difficultyLevel))?.let {
                Gson().fromJson(it, LevelDetailResponse::class.java)
            }
        }
}