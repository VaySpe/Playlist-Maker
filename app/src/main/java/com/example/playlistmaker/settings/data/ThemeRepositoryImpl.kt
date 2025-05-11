package com.example.playlistmaker.settings.data

import android.content.SharedPreferences
import com.example.playlistmaker.settings.domain.ThemeRepository

class ThemeRepositoryImpl(
    private val sharedPrefs: SharedPreferences,
    private val darkModeKey: String
) : ThemeRepository {

    override fun isDarkModeEnabled(): Boolean {
        return sharedPrefs.getBoolean(darkModeKey, false)
    }

    override fun setDarkModeEnabled(enabled: Boolean) {
        sharedPrefs.edit()
            .putBoolean(darkModeKey, enabled)
            .apply()
    }
}
