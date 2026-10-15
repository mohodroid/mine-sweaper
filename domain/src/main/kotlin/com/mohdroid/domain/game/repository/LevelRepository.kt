package com.mohdroid.domain.game.repository

import com.mohdroid.domain.game.model.level.Difficulty
import com.mohdroid.domain.game.model.level.LevelConfiguration
import com.mohdroid.domain.game.model.level.LevelSummary

/** Source-independent level-loading port. Implementations belong to data. */
interface LevelRepository {
    suspend fun loadLevels(): Result<List<LevelSummary>>
    suspend fun loadLevel(difficulty: Difficulty): Result<LevelConfiguration>
}
