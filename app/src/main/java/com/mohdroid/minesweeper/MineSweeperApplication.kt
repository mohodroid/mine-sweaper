package com.mohdroid.minesweeper

import android.app.Application
import com.mohdroid.minesweeper.main.mainModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module

class MineSweeperApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MineSweeperApplication)
            modules(listOf(mainModule, module, appContext))
        }
    }
}

val appContext = module {
    single(named("MineSweeperApplication")) { androidContext() }
}
