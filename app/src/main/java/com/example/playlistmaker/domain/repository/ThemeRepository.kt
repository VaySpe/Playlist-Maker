package com.example.playlistmaker.domain.repository

interface ThemeRepository {
    fun isDarkModeEnabled(): Boolean
    fun setDarkModeEnabled(enabled: Boolean)
}
