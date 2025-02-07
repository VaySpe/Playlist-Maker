package com.example.playlistmaker.domain.usecase

interface ThemeInteract {
    fun isDarkModeEnabled(): Boolean
    fun setDarkModeEnabled(enabled: Boolean)
}