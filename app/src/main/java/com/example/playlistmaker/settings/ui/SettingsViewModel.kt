package com.example.playlistmaker.settings.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.settings.domain.ThemeInteract
import com.example.playlistmaker.sharing.domain.SharingInteractor

class SettingsViewModel(
    private val themeInteractor: ThemeInteract,
    private val sharingInteractor: SharingInteractor
) : ViewModel() {

    fun shareApp() = sharingInteractor.shareApp()
    fun openSupport() = sharingInteractor.openSupport()
    fun openTerms() = sharingInteractor.openTerms()

    private val _darkThemeEnabled = MutableLiveData<Boolean>()
    val darkThemeEnabled: LiveData<Boolean> = _darkThemeEnabled

    fun loadTheme() {
        _darkThemeEnabled.value = themeInteractor.isDarkModeEnabled()
    }

    fun switchTheme(enabled: Boolean) {
        themeInteractor.setDarkModeEnabled(enabled)
        _darkThemeEnabled.value = enabled
    }
}
