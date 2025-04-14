package com.example.playlistmaker.search.domain

interface TracksRepository {
    suspend fun searchTracks(query: String): List<Track>
    fun getHistory(): List<Track>
    fun saveHistory(history: List<Track>)
}