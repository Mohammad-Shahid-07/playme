package com.playme.player.di

import com.playme.player.core.database.PlayMeDatabase
import com.playme.player.core.player.PlayMePlayerController
import com.playme.player.core.repository.VideoRepository
import com.playme.player.core.settings.SettingsManager
import com.playme.player.feature.library.LibraryViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { SettingsManager(androidContext()) }
    single { PlayMeDatabase.getDatabase(androidContext()) }
    single { VideoRepository(androidContext(), get()) }
    single { PlayMePlayerController(androidContext(), get(), get()) }
    viewModel { LibraryViewModel(get(), get()) }
}
