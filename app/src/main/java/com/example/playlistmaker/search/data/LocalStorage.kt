package com.example.playlistmaker.search.data

import com.example.playlistmaker.search.domain.Track

interface LocalStorage {
    fun loadHistory(): List<Track>
    fun saveHistory(tracks: List<Track>)
}