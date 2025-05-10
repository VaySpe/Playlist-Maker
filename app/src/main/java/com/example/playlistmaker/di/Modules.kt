package com.example.playlistmaker.di

import com.example.playlistmaker.player.domain.AudioPlayerImpl
import com.example.playlistmaker.player.domain.AudioPlayerInteract
import com.example.playlistmaker.player.ui.PlayerViewModel
import com.example.playlistmaker.player.ui.PlayerRepository
import com.example.playlistmaker.player.data.MediaPlayerRepositoryImpl
import com.example.playlistmaker.search.data.*
import com.example.playlistmaker.search.domain.*
import com.example.playlistmaker.search.ui.*
import com.example.playlistmaker.settings.data.ThemeRepositoryImpl
import com.example.playlistmaker.settings.domain.*
import com.example.playlistmaker.settings.ui.SettingsViewModel
import com.example.playlistmaker.sharing.data.SharingInteractorImpl
import com.example.playlistmaker.sharing.domain.SharingInteractor
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.Gson
import android.content.Context
import com.example.playlistmaker.R


val dataModule = module {

    single<ITunesApi> {
        Retrofit.Builder()
            .baseUrl("https://itunes.apple.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ITunesApi::class.java)
    }

    single { Gson() }

    single<LocalStorage> { SharedPrefsLocalStorage(get(), get()) }

    single<TracksRepository> { TracksRepositoryImpl(get(), get()) }

    single<ThemeRepository> {
        val prefs = androidApplication().getSharedPreferences("playlist_maker_preferences", Context.MODE_PRIVATE)
        ThemeRepositoryImpl(prefs, "key_for_dark_mode")
    }

    single<PlayerRepository> { MediaPlayerRepositoryImpl(get()) }

    single<SharingInteractor> { SharingInteractorImpl(get()) }
}

val domainModule = module {
    single { SearchTracksUseCase(get()) }
    single<HistoryInteract> { HistoryImpl(get()) }
    single<AudioPlayerInteract> { AudioPlayerImpl(get()) }
    single<ThemeInteract> { ThemeImpl(get()) }
    single { DpToPxUseCase() }
}

val presentationModule = module {
    viewModel { SearchViewModel(get(), get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { PlayerViewModel(get(), get(), androidApplication().getString(R.string.full_track_time)) }
}
