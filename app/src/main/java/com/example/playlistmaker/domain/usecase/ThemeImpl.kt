package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.repository.ThemeRepository

class ThemeImpl(private val themeRepository: ThemeRepository): ThemeInteract {

    override fun isDarkModeEnabled(): Boolean {
        return themeRepository.isDarkModeEnabled()
    }

    override fun setDarkModeEnabled(enabled: Boolean) {
        themeRepository.setDarkModeEnabled(enabled)
    }
}
