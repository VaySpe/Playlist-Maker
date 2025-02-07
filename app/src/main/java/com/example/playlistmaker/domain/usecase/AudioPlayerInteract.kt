package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.models.PlayerState

interface AudioPlayerInteract {
    suspend fun prepare(previewUrl: String)
    fun play()
    fun pause()
    fun release()
    fun getCurrentPositionMs(): Int
    fun getState(): PlayerState
}