package com.mohdroid.game.domain.model

data class BoardSize(val rows: Int, val columns: Int) {
    init {
        require(rows > 0) { "Board rows must be positive." }
        require(columns > 0) { "Board columns must be positive." }
    }

    fun contains(position: Position): Boolean =
        position.row in 0 until rows && position.column in 0 until columns
}
