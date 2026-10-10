package com.mohdroid.minesweeper

import android.content.Context
import org.koin.dsl.module

interface ConfigManager {

    fun readText(filename: String): Result<String>
}

class MsConfigManager(private val context: Context) : ConfigManager {

    override fun readText(filename: String): Result<String> = runCatching {
        context.assets.open(filename).bufferedReader().use { it.readText() }
    }

}

val module = module {
    factory<ConfigManager> {
        MsConfigManager(get())
    }
}
