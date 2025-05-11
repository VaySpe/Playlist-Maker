package com.example.playlistmaker.search.domain

interface HistoryInteract {
    fun getHistory(): List<Track>
    fun addTrackToHistory(track: Track)
    fun clearHistory()
}