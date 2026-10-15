package com.mohdroid.game.domain.engine

import com.mohdroid.game.domain.model.*

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Test

class GameEngineTest {

    @Test
    fun `locked exit and boundary moves do not advance the game`() {
        val session = GameEngine.create(simpleLevel()).getOrThrow()

        assertEquals(0, session.move(Direction.WEST).moves)
        session.move(Direction.EAST)
        val lockedExit = session.move(Direction.EAST)
        assertEquals(Position(0, 1), lockedExit.player)
        assertEquals(1, lockedExit.moves)
        assertEquals(GamePhase.PLAYING, lockedExit.phase)
    }

    @Test
    fun `collecting every key unlocks the exit and wins`() {
        val session = GameEngine.create(simpleLevel()).getOrThrow()

        session.move(Direction.SOUTH)
        session.move(Direction.NORTH)
        session.move(Direction.EAST)
        val won = session.move(Direction.EAST)

        assertEquals(1, won.foundKeys)
        assertEquals(4, won.moves)
        assertEquals(GamePhase.WON, won.phase)
        assertTrue(won.exitUnlocked)
    }

    @Test
    fun `a seed produces a reproducible board and generated boards remain safely completable`() {
        val level = LevelDefinition(
            BoardSize(5, 5),
            listOf(
                LevelAsset(TileKind.START, 1, listOf(Position(0, 0))),
                LevelAsset(TileKind.EXIT, 1, listOf(Position(4, 4))),
                LevelAsset(TileKind.BLOCK, 3),
                LevelAsset(TileKind.MINE, 3),
                LevelAsset(TileKind.KEY, 2)
            )
        )

        val first = GameEngine.create(level, seed = 42).getOrThrow().snapshot()
        val second = GameEngine.create(level, seed = 42).getOrThrow().snapshot()

        assertEquals(first, second)
        // Creation checks the post-placement safe-path invariant for every seed.
        (0L..100L).forEach { seed ->
            val result = GameEngine.create(level, seed)
            assertTrue("Seed $seed: ${result.exceptionOrNull()?.message}", result.isSuccess)
        }
    }

    @Test
    fun `invalid level contracts are rejected before a game starts`() {
        val invalid = LevelDefinition(
            BoardSize(2, 2),
            listOf(
                LevelAsset(TileKind.START, 1, listOf(Position(0, 0))),
                LevelAsset(TileKind.EXIT, 1, listOf(Position(3, 3)))
            )
        )

        assertTrue(GameEngine.create(invalid).isFailure)
    }

    @Test
    fun `rejected moves reuse the exact snapshot`() {
        val session = GameEngine.create(simpleLevel()).getOrThrow()
        val initial = session.snapshot()

        assertSame(initial, session.move(Direction.WEST))
        assertSame(initial, session.move(Direction.NORTH))

        val besideExit = session.move(Direction.EAST)
        assertSame(besideExit, session.move(Direction.EAST))
    }

    @Test
    fun `successful moves preserve unchanged rows and previous snapshots`() {
        val session = GameEngine.create(simpleLevel()).getOrThrow()
        val initial = session.snapshot()
        val moved = session.move(Direction.EAST)

        assertNotSame(initial, moved)
        assertNotSame(initial.board[0], moved.board[0])
        assertSame(initial.board[1], moved.board[1])
        assertSame(initial.board[0][2], moved.board[0][2])
        assertTrue(initial.board[0][0].visited)
        assertTrue(moved.board[0][0].visited)
        assertFalse(initial.board[0][1].visited)
        assertTrue(moved.board[0][1].visited)

        val returned = session.move(Direction.WEST)
        assertSame(moved.board, returned.board)
        assertEquals(2, returned.moves)
    }

    @Test
    fun `hidden contents stay out of snapshots and start clues count eight neighbors`() {
        val session = GameEngine.create(clueLevel()).getOrThrow()
        val initial = session.snapshot()

        assertEquals(TileKind.START, initial.board[0][0].tile)
        assertEquals(1, initial.board[0][0].adjacentMines)
        assertEquals(1, initial.board[0][0].adjacentKeys)
        assertNull(initial.board[0][1].tile)
        assertNull(initial.board[1][1].tile)
        assertNull(initial.board[0][1].adjacentMines)

        session.move(Direction.SOUTH)
        val found = session.move(Direction.EAST)
        assertEquals(TileKind.KEY, found.board[1][1].tile)
        assertEquals(1, found.foundKeys)
        assertEquals(1, found.board[1][1].adjacentMines)
    }

    @Test
    fun `flags are editable reminders and do not stop movement or count as moves`() {
        val session = GameEngine.create(simpleLevel()).getOrThrow()
        val key = Position(1, 0)
        val initial = session.snapshot()

        val mineFlag = session.setFlag(key, FlagKind.MINE)
        assertEquals(FlagKind.MINE, mineFlag.board[1][0].flag)
        assertNull(mineFlag.board[1][0].tile)
        assertEquals(0, mineFlag.moves)
        val cleared = session.setFlag(key, FlagKind.MINE)
        assertEquals(FlagKind.NONE, cleared.board[1][0].flag)

        val keyFlag = session.setFlag(key, FlagKind.KEY)
        assertEquals(FlagKind.KEY, keyFlag.board[1][0].flag)
        assertEquals(0, keyFlag.moves)
        assertNotSame(initial, keyFlag)
        val collected = session.move(Direction.SOUTH)
        assertEquals(FlagKind.NONE, collected.board[1][0].flag)
        assertEquals(1, collected.foundKeys)
    }

    @Test
    fun `mine hit consumes one life and reveals board at game over`() {
        val session = GameEngine.create(clueLevel()).getOrThrow()
        val lost = session.move(Direction.EAST)

        assertEquals(Position(0, 0), lost.player)
        assertEquals(1, lost.moves)
        assertEquals(0, lost.remainingLives)
        assertEquals(GamePhase.LOST, lost.phase)
        assertEquals(TileKind.MINE, lost.board[0][1].tile)
        assertEquals(TileKind.KEY, lost.board[1][1].tile)
        assertSame(lost, session.move(Direction.SOUTH))
        assertSame(lost, session.setFlag(Position(0, 2), FlagKind.KEY))
    }

    @Test
    fun `extra lives allow recovery and a revealed mine cannot hurt twice`() {
        val session = GameEngine.create(twoMineLevel(), startingLives = 2).getOrThrow()
        session.setFlag(Position(0, 1), FlagKind.MINE)
        val firstHit = session.move(Direction.EAST)

        assertEquals(GamePhase.PLAYING, firstHit.phase)
        assertEquals(1, firstHit.remainingLives)
        assertEquals(Position(0, 0), firstHit.player)
        assertEquals(FlagKind.NONE, firstHit.board[0][1].flag)
        assertSame(firstHit, session.move(Direction.EAST))
        session.move(Direction.SOUTH)
        val lastHit = session.move(Direction.EAST)
        assertEquals(GamePhase.LOST, lastHit.phase)
        assertEquals(0, lastHit.remainingLives)
        assertEquals(Position(1, 0), lastHit.player)
    }

    @Test
    fun `unsafe explicit key route and invalid life count are rejected`() {
        val unsafe = LevelDefinition(
            BoardSize(3, 3),
            listOf(
                LevelAsset(TileKind.START, 1, listOf(Position(0, 0))),
                LevelAsset(TileKind.EXIT, 1, listOf(Position(2, 2))),
                LevelAsset(TileKind.BLOCK, 2, listOf(Position(1, 1), Position(1, 2))),
                LevelAsset(TileKind.MINE, 1, listOf(Position(0, 1))),
                LevelAsset(TileKind.KEY, 1, listOf(Position(0, 2)))
            )
        )
        assertTrue(GameEngine.create(unsafe).isFailure)
        assertTrue(GameEngine.create(simpleLevel(), startingLives = 0).isFailure)
    }

    @Test
    fun `a key route cannot pass through the exit while it is locked`() {
        val exitChoke = LevelDefinition(
            BoardSize(3, 3),
            listOf(
                LevelAsset(TileKind.START, 1, listOf(Position(0, 0))),
                LevelAsset(TileKind.EXIT, 1, listOf(Position(0, 1))),
                LevelAsset(TileKind.KEY, 1, listOf(Position(0, 2))),
                LevelAsset(TileKind.BLOCK, 2, listOf(Position(1, 1), Position(1, 2)))
            )
        )
        assertTrue(GameEngine.create(exitChoke).isFailure)
    }

    private fun clueLevel() = LevelDefinition(
        BoardSize(3, 3),
        listOf(
            LevelAsset(TileKind.START, 1, listOf(Position(0, 0))),
            LevelAsset(TileKind.EXIT, 1, listOf(Position(2, 2))),
            LevelAsset(TileKind.MINE, 1, listOf(Position(0, 1))),
            LevelAsset(TileKind.KEY, 1, listOf(Position(1, 1)))
        )
    )

    private fun twoMineLevel() = LevelDefinition(
        BoardSize(3, 3),
        listOf(
            LevelAsset(TileKind.START, 1, listOf(Position(0, 0))),
            LevelAsset(TileKind.EXIT, 1, listOf(Position(2, 2))),
            LevelAsset(TileKind.MINE, 2, listOf(Position(0, 1), Position(1, 1)))
        )
    )

    private fun simpleLevel() = LevelDefinition(
        BoardSize(2, 3),
        listOf(
            LevelAsset(TileKind.START, 1, listOf(Position(0, 0))),
            LevelAsset(TileKind.EXIT, 1, listOf(Position(0, 2))),
            LevelAsset(TileKind.KEY, 1, listOf(Position(1, 0)))
        )
    )
}
