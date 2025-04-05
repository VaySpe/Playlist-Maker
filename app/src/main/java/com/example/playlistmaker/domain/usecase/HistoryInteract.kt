package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.models.Track

interface HistoryInteract {
    fun getHistory(): List<Track>
    fun addTrackToHistory(track: Track)
    fun clearHistory()
}