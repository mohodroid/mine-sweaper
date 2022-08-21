package com.mohdroid.minesweeper.main.repository

import com.mohdroid.algorithm.dto.LevelDetailResponse
import com.mohdroid.minesweeper.main.DifficultyLevel

interface GameRepository {

    suspend fun getGameLevel(difficultyLevel: DifficultyLevel) : LevelDetailResponse?

}