package com.mohdroid.minesweeper.feature

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PresentationBoundaryTest {
    @Test
    fun `screens components navigation and UI models do not reference game layer types`() {
        val main = File("src/main/java/com/mohdroid/minesweeper")
        val roots = listOf(
            File(main, "feature"),
            File(main, "ui"),
            File(main, "navigation")
        )
        roots.forEach { root ->
            assertTrue("Missing source directory: $root", root.isDirectory)
            root.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
                val source = file.readText()
                assertFalse("Domain types leaked into $file", source.contains("com.mohdroid.game."))
                assertFalse("Domain aliases leaked into $file", source.contains("typealias "))
                assertFalse("Data implementation leaked into $file", source.contains("com.mohdroid.data."))
                assertFalse("Repository leaked into presentation: $file", source.contains("game.repository."))
            }
        }
    }
}
