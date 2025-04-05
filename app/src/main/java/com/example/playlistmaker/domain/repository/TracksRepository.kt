package com.example.playlistmaker.domain.repository

import com.example.playlistmaker.domain.models.Track

interface TracksRepository {
    suspend fun searchTracks(query: String): List<Track>
    fun getHistory(): List<Track>
    fun saveHistory(history: List<Track>)
}