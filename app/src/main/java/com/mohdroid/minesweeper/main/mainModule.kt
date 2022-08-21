package com.mohdroid.minesweeper.main

import com.mohdroid.minesweeper.main.repository.GameRepository
import com.mohdroid.minesweeper.main.repository.SimpleGameRepository
import com.mohdroid.minesweeper.main.viewmodel.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mainModule = module {

    viewModel {
        MainViewModel(get())
    }
    factory<GameRepository> {
        SimpleGameRepository(get())
    }

}