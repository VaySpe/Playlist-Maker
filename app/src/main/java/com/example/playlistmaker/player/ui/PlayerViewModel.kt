package com.example.playlistmaker.player.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.usecase.HistoryInteract
import com.example.playlistmaker.player.domain.AudioPlayerInteract
import com.example.playlistmaker.player.domain.PlayerState

class PlayerViewModel(
    private val audioPlayer: AudioPlayerInteract,
    private val history: HistoryInteract
) : ViewModel() {

    private val _playerState = MutableLiveData<PlayerState>()
    val playerState: LiveData<PlayerState> = _playerState

    fun play(track: Track) {
        audioPlayer.play()
        _playerState.value = PlayerState.PLAYING
        history.addTrackToHistory(track)
    }

    fun pause() {
        audioPlayer.pause()
        _playerState.value = PlayerState.PAUSED
    }

    override fun onCleared() {
        audioPlayer.release()
    }
}
