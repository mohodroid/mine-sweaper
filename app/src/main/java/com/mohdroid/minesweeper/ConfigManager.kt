package com.mohdroid.minesweeper

import android.content.Context
import org.koin.dsl.module
import java.io.IOException
import java.io.InputStream

interface ConfigManager {

    fun getJson(filename: String): String?
}

class MsConfigManager(private val context: Context) : ConfigManager {

    override fun getJson(filename: String): String? {
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

}

val module = module {
    factory<ConfigManager> {
        MsConfigManager(get())
    }
}