package com.mohdroid.game.domain.model

data class LevelDefinition(val size: BoardSize, val assets: List<LevelAsset>) {
    fun validate() {
        require(assets.isNotEmpty()) { "A level must contain assets." }
        require(assets.filter { it.tile == TileKind.START }.sumOf { it.count } == 1) {
            "A level must contain exactly one start tile."
        }
        require(assets.filter { it.tile == TileKind.EXIT }.sumOf { it.count } == 1) {
            "A level must contain exactly one exit tile."
        }

        val explicitPositions = mutableSetOf<Position>()
        var requestedTiles = 0
        assets.forEach { asset ->
            require(asset.count >= 0) { "Asset counts cannot be negative." }
            require(asset.positions.size <= asset.count) {
                "${asset.tile} has more explicit positions than its count."
            }
            asset.positions.forEach { position ->
                require(size.contains(position)) { "Position $position is outside the board." }
                require(explicitPositions.add(position)) { "Duplicate explicit position: $position." }
            }
            requestedTiles += asset.count
        }
        require(requestedTiles <= size.rows * size.columns) {
            "The level requests more tiles than the board can hold."
        }

        val start = assets.single { it.tile == TileKind.START }
        val exit = assets.single { it.tile == TileKind.EXIT }
        require(start.positions.size == 1 && exit.positions.size == 1) {
            "Start and exit positions must be explicitly defined."
        }
    }
}
