package com.mohdroid.minesweeper.main.repository

/** JSON-only transport models. They are intentionally not shared with the game domain. */
internal class LevelJsonDto {
    var rows: Int = 0
    var cols: Int = 0
    var assetDetails: List<AssetJsonDto> = emptyList()
}

internal class AssetJsonDto {
    var type: String = ""
    var count: Int = 0
    var positions: List<PositionJsonDto> = emptyList()
}

internal class PositionJsonDto {
    var x: Int = 0
    var y: Int = 0
}
