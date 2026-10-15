package com.mohdroid.data.game

import com.mohdroid.data.game.repository.AssetLevelRepository
import com.mohdroid.domain.game.model.level.Difficulty
import com.mohdroid.domain.game.model.board.TileKind
import com.mohdroid.domain.game.di.gameDomainModule
import com.mohdroid.domain.game.di.gameScopeQualifier
import com.mohdroid.domain.game.repository.LevelRepository
import com.mohdroid.domain.game.usecase.start.StartGameUseCase
import com.mohdroid.domain.game.usecase.exit.ExitGameUseCase
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class AssetLevelRepositoryTest {
    @Test fun `JSON is mapped into independent domain configuration`() = runBlocking {
        val repository = AssetLevelRepository({ validJson })
        val level = repository.loadLevel(Difficulty.EASY).getOrThrow()
        assertEquals(2, level.rows)
        assertEquals(3, level.columns)
        assertEquals(3, level.assets.size)
        assertEquals(TileKind.START, level.assets.first().tile)
        assertEquals(2, level.assets[1].positions.single().column)
    }

    @Test fun `each difficulty selects its corresponding asset`() = runBlocking {
        val filenames = mutableListOf<String>()
        val repository = AssetLevelRepository({
            filenames.add(it)
            validJson
        })
        Difficulty.values().forEach { repository.loadLevel(it).getOrThrow() }
        assertEquals(listOf("easyLevelDetail.json", "mediumLevelDetail.json", "hardLevelDetail.json"), filenames)
    }

    @Test fun `source failures are returned to domain`() = runBlocking {
        val repository = AssetLevelRepository({ error("Missing asset") })
        assertEquals("Missing asset", repository.loadLevel(Difficulty.HARD).exceptionOrNull()?.message)
    }

    @Test fun `malformed empty unknown and incomplete JSON returns failures`() = runBlocking {
        listOf("", "null", "{", "{}", validJson.replace("START", "UNKNOWN"),
            validJson.replace("\"count\": 1", "\"missingCount\": 1"),
            validJson.replace("\"x\": 0", "\"missingRow\": 0")
        ).forEach { json ->
            val result = AssetLevelRepository({ json }).loadLevel(Difficulty.EASY)
            assertTrue("Expected decoding failure for $json", result.isFailure)
        }
    }

    @Test fun `source cancellation propagates instead of returning a failed Result`() = runBlocking {
        val repository = AssetLevelRepository({ throw CancellationException("Cancelled") })
        val pending = async { repository.loadLevel(Difficulty.EASY) }
        pending.join()
        assertTrue(pending.isCancelled)
    }

    @Test fun `all bundled assets generate playable games through the domain contract`() = runBlocking {
        val repository = AssetLevelRepository({ File("src/main/assets/$it").readText() })
        val application = koinApplication {
            modules(gameDomainModule, module { single<LevelRepository> { repository } })
        }
        try {
            Difficulty.values().forEach { difficulty ->
                val level = repository.loadLevel(difficulty).getOrThrow()
                repeat(26) { generation ->
                    val scope = application.koin.createScope("$difficulty-$generation", gameScopeQualifier)
                    try {
                        val game = scope.get<StartGameUseCase>()(difficulty).getOrThrow()
                        assertEquals(level.rows, game.board.size)
                        assertEquals(level.columns, game.board.first().size)
                        assertEquals(level.assets.filter { it.tile == TileKind.KEY }.sumOf { it.count }, game.totalKeys)
                        assertEquals(difficulty, game.difficulty)
                        scope.get<ExitGameUseCase>()()
                    } finally {
                        scope.close()
                    }
                }
            }
        } finally {
            application.close()
        }
    }

    @Test fun `catalogue loads all assets and maps their difficulty dimensions and counts`() = runBlocking {
        val filenames = mutableListOf<String>()
        val repository = AssetLevelRepository({
            filenames.add(it)
            validJson
        })
        val levels = repository.loadLevels().getOrThrow()
        assertEquals(Difficulty.values().toList(), levels.map { it.difficulty })
        assertEquals(listOf("easyLevelDetail.json", "mediumLevelDetail.json", "hardLevelDetail.json"), filenames)
        levels.forEach {
            assertEquals(2, it.rows)
            assertEquals(3, it.columns)
            assertEquals(0, it.mines)
            assertEquals(1, it.keys)
        }
    }

    @Test fun `catalogue failures and cancellation follow the repository contract`() = runBlocking {
        val broken = AssetLevelRepository({ error("Missing catalogue") })
        assertEquals("Missing catalogue", broken.loadLevels().exceptionOrNull()?.message)
        val cancelled = AssetLevelRepository({ throw CancellationException("Cancelled") })
        val task = async { cancelled.loadLevels() }
        task.join()
        assertTrue(task.isCancelled)
    }

    private val validJson = """
        {
          "rows": 2, "cols": 3,
          "assetDetails": [
            { "type": "START", "count": 1, "positions": [{ "x": 0, "y": 0 }] },
            { "type": "EXIT", "count": 1, "positions": [{ "x": 0, "y": 2 }] },
            { "type": "KEY", "count": 1, "positions": [{ "x": 1, "y": 0 }] }
          ]
        }
    """.trimIndent()
}
