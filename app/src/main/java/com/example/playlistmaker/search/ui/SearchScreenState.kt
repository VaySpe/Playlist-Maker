package com.example.playlistmaker.search.ui

import com.example.playlistmaker.search.domain.Track

data class SearchScreenState(
    val isLoading: Boolean = false,
    val searchResults: List<Track> = emptyList(),
    val history: List<Track> = emptyList(),
    val errorMessage: String? = null,
    val query: String = "",
    val navigateTo: Track? = null
)
