package com.mohdroid.minesweeper.main.repository

import com.mohdroid.minesweeper.ConfigManager
import com.mohdroid.minesweeper.main.DifficultyLevel
import com.mohdroid.game.domain.GameSession
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SimpleGameRepositoryTest {

    @Test
    fun `maps a bundled level format into a validated domain level`() = runBlocking {
        val repository = SimpleGameRepository(FakeConfigManager(validLevelJson))

        val level = repository.loadLevel(DifficultyLevel.EASY).getOrThrow()

        assertEquals(2, level.size.rows)
        assertEquals(3, level.size.columns)
        assertEquals(3, level.assets.size)
    }

    @Test
    fun `returns a failure when asset reading fails`() = runBlocking {
        val repository = SimpleGameRepository(FakeConfigManager(error = IllegalStateException("Missing asset")))

        assertTrue(repository.loadLevel(DifficultyLevel.HARD).isFailure)
    }

    @Test
    fun `every bundled difficulty loads and generates a playable board`() = runBlocking {
        val repository = SimpleGameRepository(object : ConfigManager {
            override fun readText(filename: String): Result<String> = runCatching {
                File("src/main/assets/$filename").readText()
            }
        })

        DifficultyLevel.values().forEach { difficulty ->
            val level = repository.loadLevel(difficulty).getOrThrow()
            (0L..25L).forEach { seed ->
                val result = GameSession.create(level, seed = seed)
                assertTrue("$difficulty seed $seed: ${result.exceptionOrNull()?.message}", result.isSuccess)
                val board = result.getOrThrow().snapshot()
                assertEquals(level.size.rows, board.board.size)
                assertEquals(level.size.columns, board.board.first().size)
            }
        }
    }

    private class FakeConfigManager(
        private val content: String? = null,
        private val error: Throwable? = null
    ) : ConfigManager {
        override fun readText(filename: String): Result<String> =
            error?.let { Result.failure(it) } ?: Result.success(checkNotNull(content))
    }

    private companion object {
        val validLevelJson = """
            {
              "rows": 2,
              "cols": 3,
              "assetDetails": [
                { "type": "START", "count": 1, "positions": [{ "x": 0, "y": 0 }] },
                { "type": "EXIT", "count": 1, "positions": [{ "x": 0, "y": 2 }] },
                { "type": "KEY", "count": 1, "positions": [{ "x": 1, "y": 0 }] }
              ]
            }
        """.trimIndent()
    }
}
