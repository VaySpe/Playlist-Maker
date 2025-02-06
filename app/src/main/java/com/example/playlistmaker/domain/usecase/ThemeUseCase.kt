package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.repository.ThemeRepository

class ThemeUseCase(private val themeRepository: ThemeRepository) {

    fun isDarkModeEnabled(): Boolean {
        return themeRepository.isDarkModeEnabled()
    }

    fun setDarkModeEnabled(enabled: Boolean) {
        themeRepository.setDarkModeEnabled(enabled)
    }
}
