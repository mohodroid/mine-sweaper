package com.mohdroid.minesweeper.main.repository

import com.google.gson.Gson
import com.mohdroid.game.domain.BoardSize
import com.mohdroid.game.domain.LevelAsset
import com.mohdroid.game.domain.LevelDefinition
import com.mohdroid.game.domain.Position
import com.mohdroid.game.domain.TileKind
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

    override suspend fun loadLevel(difficultyLevel: DifficultyLevel): Result<LevelDefinition> =
        getBoard(difficultyLevel)


    private suspend fun getBoard(difficultyLevel: DifficultyLevel): Result<LevelDefinition> =
        withContext(dispatcherProvider.ioDispatcher()) {
            configManager.readText(getPath(difficultyLevel)).mapCatching { json ->
                val response = Gson().fromJson(json, LevelJsonDto::class.java)
                    ?: error("Level file is empty or malformed.")
                response.toDomain().also { it.validate() }
            }
        }

    private fun LevelJsonDto.toDomain(): LevelDefinition = LevelDefinition(
        size = BoardSize(rows, cols),
        assets = assetDetails.orEmpty().map { it.toDomain() }
    )

    private fun AssetJsonDto.toDomain(): LevelAsset = LevelAsset(
        tile = TileKind.valueOf(type),
        count = count,
        positions = positions.orEmpty().map { Position(it.x, it.y) }
    )
}
