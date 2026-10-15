package com.mohdroid.domain.game.usecase.level

import com.mohdroid.domain.game.model.level.LevelSummary

interface LoadLevelsUseCase {
    suspend operator fun invoke(): Result<List<LevelSummary>>
}
