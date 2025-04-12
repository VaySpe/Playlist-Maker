package com.example.playlistmaker.player.domain

import com.example.playlistmaker.presentation.library.PlayerRepository

class AudioPlayerImpl(private val playerRepository: PlayerRepository) : AudioPlayerInteract {
    override suspend fun prepare(previewUrl: String) {
        playerRepository.prepare(previewUrl)
    }

    override fun play() {
        playerRepository.play()
    }

    override fun pause() {
        playerRepository.pause()
    }

    override fun release() {
        playerRepository.release()
    }

    override fun getCurrentPositionMs(): Int {
        return playerRepository.getCurrentPositionMs()
    }

    override fun getState(): PlayerState {
        return playerRepository.getState()
    }

    override fun setOnCompletionListener(listener: () -> Unit) {
        playerRepository.setOnCompletionListener(listener)
    }
}
