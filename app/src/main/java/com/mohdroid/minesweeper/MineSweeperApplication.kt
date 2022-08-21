package com.mohdroid.minesweeper

import android.app.Application
import android.content.Context
import com.mohdroid.minesweeper.main.mainModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.io.IOException
import java.io.InputStream

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

fun loadJSONFromAsset(context: Context, filename: String): String? {
    return try {
        val inputStream: InputStream = context.assets.open(filename)
        val size: Int = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()
        String(buffer)
    } catch (ex: IOException) {
        ex.printStackTrace()
        return null
    }
}