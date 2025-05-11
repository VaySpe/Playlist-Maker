package com.example.playlistmaker.settings.domain

interface ThemeInteract {
    fun isDarkModeEnabled(): Boolean
    fun setDarkModeEnabled(enabled: Boolean)
}