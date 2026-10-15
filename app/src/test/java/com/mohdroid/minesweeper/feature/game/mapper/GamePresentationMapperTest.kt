package com.mohdroid.minesweeper.feature.game.mapper

import com.mohdroid.domain.game.model.board.*
import com.mohdroid.domain.game.model.level.*
import com.mohdroid.domain.game.model.session.*
import com.mohdroid.minesweeper.feature.game.model.*
import com.mohdroid.minesweeper.feature.game.contract.*
import org.junit.Assert.*
import org.junit.Test
import com.mohdroid.domain.game.repository.LevelRepository
import com.mohdroid.domain.game.di.gameDomainModule
import com.mohdroid.domain.game.di.gameScopeQualifier
import com.mohdroid.domain.game.usecase.start.StartGameUseCase
import com.mohdroid.domain.game.usecase.interaction.GameInteraction
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.junit.After
import kotlinx.coroutines.runBlocking

class GamePresentationMapperTest {
    private val applications = mutableListOf<KoinApplication>()

    @After fun closeApplications() {
        applications.forEach { it.close() }
    }

    @Test
    fun `hidden mines keys and floor share the same covered UI state`() {
        val initial = createGame(clueLevel()).getOrThrow().snapshot()
        val ui = GamePresentationMapper().map(initial)

        assertEquals(ui.cells[0][1], ui.cells[1][1])
        assertEquals(ui.cells[0][1], ui.cells[0][2])
        assertEquals(CellContentUiModel.COVERED, ui.cells[0][1].content)
        assertTrue(ui.cells[0][1].canSetReminder)
        assertNull(ui.cells[0][1].mineClue)
        assertEquals("covered cell", ui.cells[0][1].accessibilityContent)
        assertFalse(ui.cells[0][0].canSetReminder)
        assertFalse(ui.cells[2][2].canSetReminder)
        assertEquals(1, ui.cells[0][0].mineClue)
        assertEquals(1, ui.cells[0][0].keyClue)
    }

    @Test
    fun `collected keys and unlocked exits are resolved before rendering`() {
        val engine = createGame(clueLevel()).getOrThrow()
        val mapper = GamePresentationMapper()
        val initial = mapper.map(engine.snapshot())
        engine.move(Direction.SOUTH)
        val domain = engine.move(Direction.EAST)
        val ui = mapper.map(domain)

        assertEquals(CellContentUiModel.LOCKED_EXIT, initial.cells[2][2].content)
        assertEquals(CellContentUiModel.OPEN_EXIT, ui.cells[2][2].content)
        assertEquals(CellContentUiModel.COLLECTED_KEY, ui.cells[1][1].content)
        assertEquals(CellPositionUiModel(1, 1), ui.player)
        assertEquals(1, ui.stats.foundKeys)
        assertEquals(2, ui.stats.moves)
        assertEquals(1, ui.cells[1][1].mineClue)
        assertFalse(ui.cells[1][1].canSetReminder)
        assertEquals(3, ui.rows)
        assertEquals(3, ui.columns)
    }

    @Test
    fun `mapping preserves unchanged rows cells and previous UI values`() {
        val engine = createGame(simpleLevel()).getOrThrow()
        val mapper = GamePresentationMapper()
        val initial = mapper.map(engine.snapshot())
        val moved = mapper.map(engine.move(Direction.EAST))

        assertNotSame(initial.cells[0], moved.cells[0])
        assertSame(initial.cells[1], moved.cells[1])
        assertSame(initial.cells[0][0], moved.cells[0][0])
        assertSame(initial.cells[0][2], moved.cells[0][2])
        assertEquals(CellContentUiModel.COVERED, initial.cells[0][1].content)
        assertEquals(CellContentUiModel.VISITED, moved.cells[0][1].content)

        val returned = mapper.map(engine.move(Direction.WEST))
        assertSame(moved.cells, returned.cells)
        assertEquals(CellPositionUiModel(0, 0), returned.player)
        assertEquals(2, returned.stats.moves)
    }

    @Test
    fun `same domain snapshot reuses the UI model and reset starts a fresh mapping`() {
        val engine = createGame(simpleLevel()).getOrThrow()
        val mapper = GamePresentationMapper()
        val initial = mapper.map(engine.snapshot())

        assertSame(initial, mapper.map(engine.move(Direction.WEST)))
        mapper.reset()
        assertNotSame(initial, mapper.map(engine.snapshot()))
    }

    @Test
    fun `reminders map without exposing the underlying tile or allocating unrelated rows`() {
        val engine = createGame(simpleLevel()).getOrThrow()
        val mapper = GamePresentationMapper()
        val initial = mapper.map(engine.snapshot())
        val mine = mapper.map(engine.setFlag(Position(1, 0), Reminder.MINE))
        assertEquals(ReminderUiModel.MINE, mine.cells[1][0].reminder)
        assertEquals(CellContentUiModel.COVERED, mine.cells[1][0].content)
        assertSame(initial.cells[0], mine.cells[0])
        assertSame(initial.cells[1][1], mine.cells[1][1])
        assertEquals(ReminderUiModel.NONE, initial.cells[1][0].reminder)

        val key = mapper.map(engine.setFlag(Position(1, 0), Reminder.KEY))
        assertEquals(ReminderUiModel.KEY, key.cells[1][0].reminder)
        assertEquals(0, key.stats.moves)
        val cleared = mapper.map(engine.setFlag(Position(1, 0), Reminder.NONE))
        assertEquals(ReminderUiModel.NONE, cleared.cells[1][0].reminder)
    }

    @Test
    fun `loss exposes review visuals and disables every reminder target`() {
        val engine = createGame(clueLevel()).getOrThrow()
        engine.setFlag(Position(1, 1), Reminder.KEY)
        val mapper = GamePresentationMapper()
        mapper.map(engine.snapshot())
        val lost = mapper.map(engine.move(Direction.EAST))

        assertEquals(GameStatusUiModel.LOST, lost.status)
        assertEquals(0, lost.stats.remainingLives)
        assertEquals(CellPositionUiModel(0, 0), lost.player)
        assertEquals(CellContentUiModel.MINE, lost.cells[0][1].content)
        assertEquals(CellContentUiModel.KEY, lost.cells[1][1].content)
        assertEquals(ReminderUiModel.KEY, lost.cells[1][1].reminder)
        assertTrue(lost.cells.flatten().none { it.canSetReminder })
        val state = GameUiState(GameLoadState.READY, game = lost)
        assertFalse(state.canMove)
        assertTrue(state.isFinished)
    }

    @Test
    fun `a mine hit with another life keeps gameplay enabled`() {
        val snapshot = GameState(
            difficulty = Difficulty.EASY, player = Position(0, 0),
            moves = 1, totalKeys = 1, foundKeys = 0, mines = 1,
            startingLives = 2, remainingLives = 1, phase = GamePhase.PLAYING,
            board = listOf(
                listOf(CellState(TileKind.START, visited = true), CellState(TileKind.MINE)),
                listOf(CellState(null), CellState(null))
            )
        )
        val ui = GamePresentationMapper().map(snapshot)

        assertEquals(GameStatusUiModel.IN_PROGRESS, ui.status)
        assertEquals(1, ui.stats.remainingLives)
        assertEquals(2, ui.stats.startingLives)
        assertEquals(1, ui.stats.mines)
        assertEquals(CellContentUiModel.MINE, ui.cells[0][1].content)
        assertFalse(ui.cells[0][1].canSetReminder)
        assertTrue(ui.cells[1][1].canSetReminder)
        assertTrue(GameUiState(GameLoadState.READY, game = ui).canMove)
    }

    @Test
    fun `victory maps to a finished UI state`() {
        val engine = createGame(simpleLevel()).getOrThrow()
        engine.move(Direction.SOUTH)
        engine.move(Direction.NORTH)
        engine.move(Direction.EAST)
        val ui = GamePresentationMapper().map(engine.move(Direction.EAST))

        assertEquals(GameStatusUiModel.WON, ui.status)
        assertEquals(CellContentUiModel.OPEN_EXIT, ui.cells[0][2].content)
        assertFalse(GameUiState(GameLoadState.READY, game = ui).canMove)
        assertTrue(GameUiState(GameLoadState.READY, game = ui).isFinished)
    }

    @Test
    fun `all visible tile kinds have an explicit visual mapping`() {
        val snapshot = GameState(difficulty = Difficulty.EASY,
            player = Position(0, 0), moves = 0, totalKeys = 1, foundKeys = 0,
            mines = 1, startingLives = 1, remainingLives = 1, phase = GamePhase.PLAYING,
            board = listOf(listOf(
                CellState(TileKind.START),
                CellState(TileKind.START, visited = true),
                CellState(TileKind.STEP),
                CellState(TileKind.STEP, visited = true),
                CellState(TileKind.BLOCK),
                CellState(TileKind.MINE),
                CellState(TileKind.KEY),
                CellState(TileKind.KEY, visited = true),
                CellState(TileKind.EXIT)
            ))
        )
        val ui = GamePresentationMapper().map(snapshot)
        assertEquals(
            listOf(CellContentUiModel.START, CellContentUiModel.VISITED,
                CellContentUiModel.COVERED, CellContentUiModel.VISITED,
                CellContentUiModel.BLOCK, CellContentUiModel.MINE,
                CellContentUiModel.KEY, CellContentUiModel.COLLECTED_KEY,
                CellContentUiModel.LOCKED_EXIT),
            ui.cells.single().map { it.content }
        )
    }

    private fun simpleLevel() = LevelConfiguration(2, 3, listOf(
        LevelAsset(TileKind.START, 1, listOf(Position(0, 0))),
        LevelAsset(TileKind.EXIT, 1, listOf(Position(0, 2))),
        LevelAsset(TileKind.KEY, 1, listOf(Position(1, 0)))
    ))

    private fun clueLevel() = LevelConfiguration(3, 3, listOf(
        LevelAsset(TileKind.START, 1, listOf(Position(0, 0))),
        LevelAsset(TileKind.EXIT, 1, listOf(Position(2, 2))),
        LevelAsset(TileKind.MINE, 1, listOf(Position(0, 1))),
        LevelAsset(TileKind.KEY, 1, listOf(Position(1, 1)))
    ))

    // Resolve public contracts through real DI; app tests still have no engine dependency.
    private fun createGame(level: LevelConfiguration): Result<GameFixture> = runCatching {
        val repository = object : LevelRepository {
            override suspend fun loadLevel(difficulty: Difficulty) = Result.success(level)
            override suspend fun loadLevels(): Result<List<LevelSummary>> = error("Unexpected catalogue load")
        }
        val application = koinApplication {
            modules(gameDomainModule, module { single<LevelRepository> { repository } })
        }
        applications.add(application)
        val scope = application.koin.createScope("mapper-test", gameScopeQualifier)
        val state = runBlocking { scope.get<StartGameUseCase>()(Difficulty.EASY).getOrThrow() }
        GameFixture(scope.get(), state)
    }

    private class GameFixture(private val interaction: GameInteraction, private var state: GameState) {
        fun snapshot() = state
        fun move(direction: Direction): GameState =
            interaction.movePlayer(direction).getOrThrow().also { state = it }
        fun setFlag(position: Position, reminder: Reminder): GameState =
            interaction.setCellReminder(position, reminder).getOrThrow().also { state = it }
    }
}
