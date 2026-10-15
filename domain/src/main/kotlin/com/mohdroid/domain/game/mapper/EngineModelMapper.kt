package com.mohdroid.domain.game.mapper

import com.mohdroid.domain.game.model.level.LevelConfiguration
import com.mohdroid.domain.game.model.level.LevelAsset
import com.mohdroid.domain.game.model.board.Position
import com.mohdroid.domain.game.model.board.TileKind
import com.mohdroid.domain.game.model.board.Direction
import com.mohdroid.domain.game.model.board.Reminder
import com.mohdroid.game.domain.model.BoardSize
import com.mohdroid.game.domain.model.LevelDefinition

internal fun LevelConfiguration.toEngine(): LevelDefinition = LevelDefinition(
    BoardSize(rows, columns),
    assets.map { asset ->
        com.mohdroid.game.domain.model.LevelAsset(
            asset.tile.toEngine(), asset.count, asset.positions.map { it.toEngine() }
        )
    }
)

internal fun TileKind.toEngine(): com.mohdroid.game.domain.model.TileKind = when (this) {
    TileKind.START -> com.mohdroid.game.domain.model.TileKind.START
    TileKind.EXIT -> com.mohdroid.game.domain.model.TileKind.EXIT
    TileKind.BLOCK -> com.mohdroid.game.domain.model.TileKind.BLOCK
    TileKind.MINE -> com.mohdroid.game.domain.model.TileKind.MINE
    TileKind.KEY -> com.mohdroid.game.domain.model.TileKind.KEY
    TileKind.STEP -> com.mohdroid.game.domain.model.TileKind.STEP
}

internal fun Position.toEngine() = com.mohdroid.game.domain.model.Position(row, column)

internal fun Direction.toEngine(): com.mohdroid.game.domain.model.Direction = when (this) {
    Direction.NORTH -> com.mohdroid.game.domain.model.Direction.NORTH
    Direction.SOUTH -> com.mohdroid.game.domain.model.Direction.SOUTH
    Direction.EAST -> com.mohdroid.game.domain.model.Direction.EAST
    Direction.WEST -> com.mohdroid.game.domain.model.Direction.WEST
}

internal fun Reminder.toEngine(): com.mohdroid.game.domain.model.FlagKind = when (this) {
    Reminder.NONE -> com.mohdroid.game.domain.model.FlagKind.NONE
    Reminder.MINE -> com.mohdroid.game.domain.model.FlagKind.MINE
    Reminder.KEY -> com.mohdroid.game.domain.model.FlagKind.KEY
}
