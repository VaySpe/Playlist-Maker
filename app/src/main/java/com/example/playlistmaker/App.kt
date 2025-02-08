package com.example.playlistmaker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.domain.usecase.ThemeInteract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class App : Application() {

    var darkTheme = false
    private lateinit var themeUseCase: ThemeInteract

    // 1) Делаем appScope полем (val), а не локальной переменной
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        Creator.init(this)

        // 2) Инициализируем UseCase для работы с темой
        themeUseCase = Creator.provideThemeUseCase()

        // 3) Устанавливаем тему при старте
        switchTheme(themeUseCase.isDarkModeEnabled())
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        darkTheme = darkThemeEnabled
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
