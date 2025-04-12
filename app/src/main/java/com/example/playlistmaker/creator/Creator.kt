package com.example.playlistmaker.creator

import android.app.Application
import android.content.Context
import com.example.playlistmaker.data.network.ITunesApi
import com.example.playlistmaker.player.data.MediaPlayerRepositoryImpl
import com.example.playlistmaker.settings.data.ThemeRepositoryImpl
import com.example.playlistmaker.data.repository.TracksRepositoryImpl
import com.example.playlistmaker.data.storage.LocalStorage
import com.example.playlistmaker.data.storage.SharedPrefsLocalStorage
import com.example.playlistmaker.settings.domain.ThemeRepository
import com.example.playlistmaker.domain.repository.TracksRepository
import com.example.playlistmaker.domain.usecase.*
import com.example.playlistmaker.player.domain.AudioPlayerImpl
import com.example.playlistmaker.settings.domain.ThemeImpl
import com.example.playlistmaker.settings.domain.ThemeInteract
import com.example.playlistmaker.settings.ui.SettingsViewModel
import com.example.playlistmaker.sharing.data.SharingInteractorImpl
import com.example.playlistmaker.sharing.domain.SharingInteractor
import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Creator {
    private val dpToPxUseCase by lazy { DpToPxUseCase() }
    private lateinit var appContext: Application

    fun init(application: Application) {
        appContext = application
    }

    // ------------------ Gson ------------------
    val gson: Gson by lazy { Gson() }

    // ------------------ Retrofit ------------------
    private val iTunesApi: ITunesApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://itunes.apple.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ITunesApi::class.java)
    }

    // ------------------ SharedPrefs (LocalStorage) ------------------
    private val localStorage: LocalStorage by lazy {
        SharedPrefsLocalStorage(appContext)
    }

    // ------------------ Tracks Repository ------------------
    private val tracksRepository: TracksRepository by lazy {
        TracksRepositoryImpl(
            iTunesApi = iTunesApi,
            localStorage = localStorage
        )
    }

    // ------------------ Theme Repository ------------------
    private const val PLAYLIST_MAKER_PREFERENCES = "playlist_maker_preferences"
    private const val DARK_MODE_KEY = "key_for_dark_mode"

    private val themeRepository: ThemeRepository by lazy {
        val sharedPrefs = appContext.getSharedPreferences(PLAYLIST_MAKER_PREFERENCES, Context.MODE_PRIVATE)
        ThemeRepositoryImpl(sharedPrefs, DARK_MODE_KEY)
    }

    // ------------------ UseCases ------------------
    fun provideSearchTracksUseCase(): SearchTracksUseCase {
        return SearchTracksUseCase(tracksRepository)
    }

    fun provideHistoryUseCase(): HistoryImpl {
        return HistoryImpl(tracksRepository)
    }

    // ------------------ Audio Player ------------------
    fun provideAudioPlayerUseCase(): AudioPlayerImpl {
        return AudioPlayerImpl(MediaPlayerRepositoryImpl(appContext))
    }

    // ------------------ Theme UseCase ------------------
    private val themeUseCase by lazy {
        ThemeImpl(themeRepository)
    }

    fun provideThemeUseCase(): ThemeImpl {
        return themeUseCase
    }

    // ------------------ DpToPx UseCase ------------------
    fun provideDpToPxUseCase(): DpToPxUseCase {
        return dpToPxUseCase
    }

    fun provideSettingsViewModel(): SettingsViewModel {
        return SettingsViewModel(
            provideThemeInteractor(),
            provideSharingInteractor()
        )
    }



    fun provideThemeInteractor(): ThemeInteract {
        return ThemeImpl(themeRepository)
    }

    private val sharingInteractor: SharingInteractor by lazy {
        SharingInteractorImpl(appContext)
    }

    fun provideSharingInteractor(): SharingInteractor = sharingInteractor

}
