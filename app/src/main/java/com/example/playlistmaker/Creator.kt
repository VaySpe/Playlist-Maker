package com.example.playlistmaker

import android.app.Application
import android.content.Context
import com.example.playlistmaker.data.network.ITunesApi
import com.example.playlistmaker.data.repository.MediaPlayerRepositoryImpl
import com.example.playlistmaker.data.repository.ThemeRepositoryImpl
import com.example.playlistmaker.data.repository.TracksRepositoryImpl
import com.example.playlistmaker.data.storage.LocalStorage
import com.example.playlistmaker.data.storage.SharedPrefsLocalStorage
import com.example.playlistmaker.domain.repository.ThemeRepository
import com.example.playlistmaker.domain.repository.TracksRepository
import com.example.playlistmaker.domain.usecase.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Creator {

    private lateinit var appContext: Application

    // Вызываем из App.onCreate()
    fun init(application: Application) {
        appContext = application
    }

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
        // Если хотите использовать тот же SharedPrefs, что и для локального хранения,
        // можно передать (localStorage as SharedPrefsLocalStorage).sharedPrefs
        // Но здесь для наглядности берём новый:
        val sharedPrefs = appContext.getSharedPreferences(PLAYLIST_MAKER_PREFERENCES, Context.MODE_PRIVATE)
        ThemeRepositoryImpl(sharedPrefs, DARK_MODE_KEY)
    }

    // ------------------ UseCases ------------------
    fun provideSearchTracksUseCase(): SearchTracksUseCase {
        return SearchTracksUseCase(tracksRepository)
    }

    fun provideHistoryUseCase(): HistoryUseCase {
        return HistoryUseCase(tracksRepository)
    }

    // ------------------ Audio Player ------------------
    private val mediaPlayerRepository by lazy {
        MediaPlayerRepositoryImpl(appContext)
    }
    fun provideAudioPlayerUseCase(): AudioPlayerUseCase {
        return AudioPlayerUseCase(mediaPlayerRepository)
    }

    // ------------------ Theme UseCase ------------------
    private val themeUseCase by lazy {
        ThemeUseCase(themeRepository)
    }
    fun provideThemeUseCase(): ThemeUseCase {
        return themeUseCase
    }
}
