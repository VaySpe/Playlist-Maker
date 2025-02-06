package com.example.playlistmaker.presentation.search

import com.example.playlistmaker.domain.models.Track

interface SearchView {
    fun showLoading(isLoading: Boolean)
    fun showClearButton(isVisible: Boolean)

    fun showNoResults()
    fun hideNoResults()

    fun showTracks(tracks: List<Track>)
    fun hideTracks()

    fun showError(message: String)
    fun hideError()

    fun navigateToPlayer(track: Track)
    fun showHistory(tracks: List<Track>)
    fun hideHistory()
}
