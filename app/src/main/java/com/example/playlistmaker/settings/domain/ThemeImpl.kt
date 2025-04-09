package com.example.playlistmaker.settings.domain

class ThemeImpl(private val themeRepository: ThemeRepository): ThemeInteract {

    override fun isDarkModeEnabled(): Boolean {
        return themeRepository.isDarkModeEnabled()
    }

    override fun setDarkModeEnabled(enabled: Boolean) {
        themeRepository.setDarkModeEnabled(enabled)
    }
}
