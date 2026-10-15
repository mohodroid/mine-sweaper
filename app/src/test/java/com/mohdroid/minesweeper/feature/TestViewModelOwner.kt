package com.mohdroid.minesweeper.feature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore

internal class TestViewModelOwner {
    private val store = ViewModelStore()
    private var nextKey = 0

    fun <T : ViewModel> keep(viewModel: T): T =
        ViewModelProvider(store, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <V : ViewModel> create(modelClass: Class<V>): V = viewModel as V
        }).get("test-${nextKey++}", viewModel.javaClass)

    fun clear() = store.clear()
}
