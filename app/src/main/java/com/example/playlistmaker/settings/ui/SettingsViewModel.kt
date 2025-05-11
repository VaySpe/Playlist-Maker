package com.example.playlistmaker.settings.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.settings.domain.ThemeInteract

class SettingsViewModel(
    private val themeInteract: ThemeInteract
) : ViewModel() {

    private val _screenState = MutableLiveData<SettingsScreenState>()
    val screenState: LiveData<SettingsScreenState> = _screenState

    private val _events = MutableLiveData<SettingsEvent?>()
    val events: LiveData<SettingsEvent?> = _events

    fun loadTheme() {
        _screenState.value = SettingsScreenState(
            isDarkTheme = themeInteract.isDarkModeEnabled()
        )
    }

    fun switchTheme(isDark: Boolean) {
        themeInteract.setDarkModeEnabled(isDark)
        _screenState.value = _screenState.value?.copy(isDarkTheme = isDark)
            ?: SettingsScreenState(isDarkTheme = isDark)
    }

    fun shareApp() {
        _events.value = SettingsEvent.ShareApp
    }

    fun openSupport() {
        _events.value = SettingsEvent.OpenSupport
    }

    fun openTerms() {
        _events.value = SettingsEvent.OpenTerms
    }

    fun onEventHandled() {
        _events.value = null
    }
}
