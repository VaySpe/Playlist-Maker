package com.example.playlistmaker.settings.domain

interface ThemeRepository {
    fun isDarkModeEnabled(): Boolean
    fun setDarkModeEnabled(enabled: Boolean)
}
