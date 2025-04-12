package com.example.playlistmaker.player.domain

interface AudioPlayerInteract {
    suspend fun prepare(previewUrl: String)
    fun play()
    fun pause()
    fun release()
    fun getCurrentPositionMs(): Int
    fun getState(): PlayerState
}