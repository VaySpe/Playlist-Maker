package com.example.playlistmaker.domain.repository

import com.example.playlistmaker.domain.models.PlayerState

interface PlayerRepository {
    suspend fun prepare(previewUrl: String)
    fun play()
    fun pause()
    fun release()
    fun getCurrentPositionMs(): Int
    fun getState(): PlayerState
}