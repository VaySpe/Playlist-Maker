package com.example.playlistmaker.player.ui

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.search.domain.Track
import com.example.playlistmaker.search.domain.HistoryInteract
import com.example.playlistmaker.player.domain.AudioPlayerInteract
import com.example.playlistmaker.player.domain.PlayerState
import java.util.Locale

class PlayerViewModel(
    private val audioPlayer: AudioPlayerInteract,
    private val history: HistoryInteract,
    // Значение длительности preview (например, "00:30"), передаётся из ресурсов через фабрику ViewModel
    private val defaultTimerText: String
) : ViewModel() {

    private val _playerState = MutableLiveData<PlayerState>()
    val playerState: LiveData<PlayerState> = _playerState

    private val _timerText = MutableLiveData<String>()
    val timerText: LiveData<String> = _timerText

    private var currentTrack: Track? = null

    private val handler = Handler(Looper.getMainLooper())
    private val updateTimerRunnable = object : Runnable {
        override fun run() {
            if (audioPlayer.getState() == PlayerState.PLAYING) {
                val currentMs = audioPlayer.getCurrentPositionMs()
                _timerText.value = formatTime(currentMs)
                handler.postDelayed(this, 200L)
            } else if (audioPlayer.getState() == PlayerState.PREPARED) {
                pause()
                _timerText.value = defaultTimerText
            }
        }
    }

    // Подготовка плеера (вызывается из корутины)
    suspend fun prepare(track: Track) {
        currentTrack = track
        _timerText.value = defaultTimerText
        audioPlayer.prepare(track.previewUrl)
        _playerState.value = PlayerState.PREPARED
    }

    fun play() {
        handler.removeCallbacks(updateTimerRunnable)
        audioPlayer.play()
        _playerState.value = PlayerState.PLAYING
        currentTrack?.let { history.addTrackToHistory(it) }
        handler.post(updateTimerRunnable)
    }

    fun pause() {
        handler.removeCallbacks(updateTimerRunnable)
        audioPlayer.pause()
        _playerState.value = PlayerState.PAUSED
    }

    fun togglePlayPause() {
        when (_playerState.value) {
            PlayerState.PLAYING -> pause()
            PlayerState.PREPARED, PlayerState.PAUSED -> play()
            else -> {}
        }
    }

    // Форматирование времени (мс → "мм:сс")
    private fun formatTime(ms: Int): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    override fun onCleared() {
        audioPlayer.release()
        handler.removeCallbacks(updateTimerRunnable)
        super.onCleared()
    }
}
