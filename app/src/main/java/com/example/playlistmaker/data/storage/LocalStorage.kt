package com.example.playlistmaker.data.storage

import com.example.playlistmaker.domain.models.Track

interface LocalStorage {
    fun loadHistory(): List<Track>
    fun saveHistory(tracks: List<Track>)
    // ... любые другие методы
}