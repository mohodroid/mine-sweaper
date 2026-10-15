package com.mohdroid.data.game.model

/** Nullable transport fields let the mapper reject missing/malformed JSON explicitly. */
internal class LevelJsonDto {
    var rows: Int? = null
    var cols: Int? = null
    var assetDetails: List<AssetJsonDto>? = null
}

internal class AssetJsonDto {
    var type: String? = null
    var count: Int? = null
    var positions: List<PositionJsonDto>? = null
}

internal class PositionJsonDto {
    var x: Int? = null
    var y: Int? = null
}
