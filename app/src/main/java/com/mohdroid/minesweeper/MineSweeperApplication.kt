package com.mohdroid.minesweeper

import android.app.Application
import com.mohdroid.data.game.di.gameDataModule
import com.mohdroid.domain.game.di.gameDomainModule
import com.mohdroid.minesweeper.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class MineSweeperApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MineSweeperApplication)
            modules(gameDataModule, gameDomainModule, appModule)
        }
    }
}
