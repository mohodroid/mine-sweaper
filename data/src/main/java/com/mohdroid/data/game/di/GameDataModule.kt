package com.mohdroid.data.game.di

import android.content.Context
import com.mohdroid.data.game.repository.AssetLevelRepository
import com.mohdroid.data.game.repository.readLevelAsset
import com.mohdroid.domain.game.repository.LevelRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module

private val assetIoDispatcherQualifier = named("game.assetIoDispatcher")

val gameDataModule = module {
    single<CoroutineDispatcher>(assetIoDispatcherQualifier) { Dispatchers.IO }
    single<LevelRepository> {
        val assets = get<Context>().applicationContext.assets
        AssetLevelRepository(
            readAssetText = { filename -> readLevelAsset(assets, filename) },
            ioDispatcher = get(assetIoDispatcherQualifier)
        )
    }
}
