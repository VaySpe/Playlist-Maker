package com.example.playlistmaker

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.getDefaultNightMode

class App : Application() {

    var darkTheme = false
    private lateinit var sharedPreferencesMode: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        sharedPreferencesMode = getSharedPreferences(PLAYLIST_MAKER_PREFERENCES, MODE_PRIVATE)

        darkTheme = sharedPreferencesMode.getBoolean(DARK_MODE_KEY,
            getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO
        )
        switchTheme(darkTheme)
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        darkTheme = darkThemeEnabled
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }
}
