package com.mohdroid.minesweeper.main

import com.mohdroid.algorithm.MineSweeper


enum class DifficultyLevel {

    EASY, MEDIUM, HARD

}

fun Array<Array<MineSweeper.Node>>.toList() : List<List<MineSweeper.Node>> {
    val list: MutableList<MutableList<MineSweeper.Node>> = mutableListOf()
    forEachIndexed { i, arrayOfNodes ->
        list.add(i, arrayOfNodes.toMutableList())
    }
    return list
}