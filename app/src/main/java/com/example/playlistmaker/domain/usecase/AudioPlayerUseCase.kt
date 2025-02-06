package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.models.PlayerState
import com.example.playlistmaker.domain.repository.PlayerRepository

class AudioPlayerUseCase(private val playerRepository: PlayerRepository) {
    suspend fun prepare(previewUrl: String){
        playerRepository.prepare(previewUrl)
    }

    fun play(){
        playerRepository.play()
    }

    fun pause() {
        playerRepository.pause()
    }

    fun release() {
        playerRepository.release()
    }

    fun getCurrentPositionMs(): Int {
        return playerRepository.getCurrentPositionMs()
    }

    fun getState(): PlayerState {
        return playerRepository.getState()
    }
}