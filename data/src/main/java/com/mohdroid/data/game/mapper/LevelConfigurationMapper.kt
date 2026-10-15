package com.mohdroid.data.game.mapper

import com.mohdroid.data.game.model.AssetJsonDto
import com.mohdroid.data.game.model.LevelJsonDto
import com.mohdroid.domain.game.model.level.LevelAsset
import com.mohdroid.domain.game.model.level.LevelConfiguration
import com.mohdroid.domain.game.model.level.Difficulty
import com.mohdroid.domain.game.model.level.LevelSummary
import com.mohdroid.domain.game.model.board.Position
import com.mohdroid.domain.game.model.board.TileKind

/** Decodes representation only; engine validation owns gameplay constraints. */
internal fun LevelJsonDto.toDomain() = LevelConfiguration(
    rows = requireNotNull(rows) { "Level rows are missing." },
    columns = requireNotNull(cols) { "Level columns are missing." },
    assets = requireNotNull(assetDetails) { "Level assets are missing." }.map { it.toDomain() }
)

private fun AssetJsonDto.toDomain() = LevelAsset(
    tile = TileKind.valueOf(requireNotNull(type) { "Asset type is missing." }),
    count = requireNotNull(count) { "Asset count is missing." },
    positions = positions.orEmpty().map {
        Position(
            requireNotNull(it.x) { "Asset row is missing." },
            requireNotNull(it.y) { "Asset column is missing." }
        )
    }
)

internal fun LevelConfiguration.toSummary(difficulty: Difficulty) = LevelSummary(
    difficulty = difficulty,
    rows = rows,
    columns = columns,
    mines = assets.filter { it.tile == TileKind.MINE }.sumOf { it.count },
    keys = assets.filter { it.tile == TileKind.KEY }.sumOf { it.count }
)
