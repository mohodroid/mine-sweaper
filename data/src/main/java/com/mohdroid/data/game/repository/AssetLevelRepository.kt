package com.mohdroid.data.game.repository

import android.content.res.AssetManager
import com.google.gson.Gson
import com.mohdroid.data.game.mapper.toDomain
import com.mohdroid.data.game.mapper.toSummary
import com.mohdroid.data.game.model.LevelJsonDto
import com.mohdroid.domain.game.model.level.Difficulty
import com.mohdroid.domain.game.model.level.LevelConfiguration
import com.mohdroid.domain.game.model.level.LevelSummary
import com.mohdroid.domain.game.repository.LevelRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AssetLevelRepository(
    private val readAssetText: (String) -> String,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : LevelRepository {
    override suspend fun loadLevels(): Result<List<LevelSummary>> = readOnIo {
        Difficulty.values().map { difficulty -> readConfiguration(difficulty).toSummary(difficulty) }
    }

    override suspend fun loadLevel(difficulty: Difficulty): Result<LevelConfiguration> =
        readOnIo { readConfiguration(difficulty) }

    private fun readConfiguration(difficulty: Difficulty): LevelConfiguration {
        val filename = when (difficulty) {
            Difficulty.EASY -> "easyLevelDetail.json"
            Difficulty.MEDIUM -> "mediumLevelDetail.json"
            Difficulty.HARD -> "hardLevelDetail.json"
        }
        val dto = Gson().fromJson(readAssetText(filename), LevelJsonDto::class.java)
            ?: error("Level file is empty or malformed.")
        return dto.toDomain()
    }

    private suspend fun <T> readOnIo(read: () -> T): Result<T> = try {
        val value = withContext(ioDispatcher) { read() }
        Result.success(value)
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception)
    }
}

/** Asset I/O stays in data; the repository's reader function can be replaced independently. */
internal fun readLevelAsset(assets: AssetManager, filename: String): String =
    assets.open(filename).bufferedReader().use { it.readText() }
