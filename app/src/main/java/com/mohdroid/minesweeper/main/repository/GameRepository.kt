package com.mohdroid.minesweeper.main.repository

import com.mohdroid.game.domain.LevelDefinition
import com.mohdroid.minesweeper.main.DifficultyLevel

interface GameRepository {

    suspend fun loadLevel(difficultyLevel: DifficultyLevel): Result<LevelDefinition>

}
