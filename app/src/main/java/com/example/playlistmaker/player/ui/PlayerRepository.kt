package com.example.playlistmaker.player.ui

import com.example.playlistmaker.player.domain.PlayerState

interface PlayerRepository {
    suspend fun prepare(previewUrl: String)
    fun play()
    fun pause()
    fun release()
    fun getCurrentPositionMs(): Int
    fun getState(): PlayerState
    fun setOnCompletionListener(listener: () -> Unit)
}