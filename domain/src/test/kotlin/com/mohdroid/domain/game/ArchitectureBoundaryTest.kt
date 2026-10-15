package com.mohdroid.domain.game

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Small regression guards supplement the boundaries enforced by Gradle. */
class ArchitectureBoundaryTest {
    @Test fun `module dependencies keep engine and storage out of presentation`() {
        fun dependencies(module: String) = File("../$module/build.gradle").readText()
        val app = dependencies("app")
        assertTrue(app.contains("project(':domain')"))
        assertTrue(app.contains("project(':data')"))
        assertFalse(app.contains(":core-game"))
        val data = dependencies("data")
        assertTrue(data.contains("project(':domain')"))
        assertFalse(data.contains(":core-game"))
        assertFalse(data.contains(":app"))
        val domain = dependencies("domain")
        assertTrue(domain.contains("implementation project(':core-game')"))
        assertFalse(domain.contains(":data"))
        assertFalse(domain.contains(":app"))
    }

    @Test fun `domain business code stays framework-free and DI stays in its package`() {
        val sources = File("src/main/kotlin")
        assertTrue(sources.isDirectory)
        val forbidden = listOf("import android.", "import androidx.", "import com.google.gson.",
            "import com.mohdroid.data.", "import com.mohdroid.minesweeper.")
        sources.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
            forbidden.forEach { assertFalse("$it leaked into $file", file.readText().contains(it)) }
            if (file.parentFile.name != "di") {
                assertFalse("Koin outside DI: $file", file.readText().contains("import org.koin."))
            }
        }
    }

    @Test fun `public contracts and models contain no engine types or aliases`() {
        val game = File("src/main/kotlin/com/mohdroid/domain/game")
        val files = File(game, "model").walkTopDown().filter { it.extension == "kt" }.toList() +
            File(game, "usecase").walkTopDown().filter {
                it.extension == "kt" && !it.name.startsWith("Default")
            }.toList() + File(game, "repository/LevelRepository.kt")
        files.forEach { file ->
            val source = file.readText()
            assertFalse("Engine leaked into $file", source.contains("com.mohdroid.game."))
            assertFalse("Alias bypassed boundary in $file", source.contains("typealias "))
        }
    }

    @Test fun `engine library contains only engine behavior and engine models`() {
        val engine = File("../core-game/src/main/kotlin")
        assertTrue(engine.isDirectory)
        engine.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
            val source = file.readText()
            assertFalse("Repository leaked into engine: $file", source.contains("Repository"))
            listOf("import android.", "import androidx.", "import org.koin.",
                "import com.mohdroid.domain.", "import com.mohdroid.data.", "import com.google.gson.")
                .forEach { assertFalse("Invalid dependency in $file", source.contains(it)) }
        }
    }

    @Test fun `data has no dependency on engine or presentation models`() {
        val data = File("../data/src/main/java")
        assertTrue(data.isDirectory)
        data.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
            val source = file.readText()
            assertFalse("Engine leaked into $file", source.contains("com.mohdroid.game."))
            assertFalse("Presentation leaked into $file", source.contains("com.mohdroid.minesweeper."))
        }
    }
}
