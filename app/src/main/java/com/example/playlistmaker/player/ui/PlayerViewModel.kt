package com.example.playlistmaker.player.ui

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.player.domain.AudioPlayerInteract
import com.example.playlistmaker.player.domain.PlayerState
import com.example.playlistmaker.search.domain.HistoryInteract
import com.example.playlistmaker.search.domain.Track
import java.util.Locale

class PlayerViewModel(
    private val audioPlayer: AudioPlayerInteract,
    private val history: HistoryInteract,
    private val defaultTimerText: String
) : ViewModel() {

    private val _screenState = MutableLiveData<PlayerScreenState>()
    val screenState: LiveData<PlayerScreenState> = _screenState

    private var currentTrack: Track? = null

    private val handler = Handler(Looper.getMainLooper())
    private val updateTimerRunnable = object : Runnable {
        override fun run() {
            when (audioPlayer.getState()) {
                PlayerState.PLAYING -> {
                    val currentMs = audioPlayer.getCurrentPositionMs()
                    updateScreenState(timerText = formatTime(currentMs))
                    handler.postDelayed(this, 200L)
                }
                PlayerState.PREPARED -> {
                    pause() // Если воспроизведение завершилось, сбрасываем таймер
                    updateScreenState(timerText = defaultTimerText)
                }
                else -> {} // Нет изменений для других состояний
            }
        }
    }

    suspend fun prepare(track: Track) {
        currentTrack = track
        audioPlayer.prepare(track.previewUrl)

        _screenState.value = PlayerScreenState(
            trackName = track.trackName,
            artistName = track.artistName,
            album = track.collectionName,
            duration = track.trackTime,
            year = track.releaseDate.substring(0, 4),
            genre = track.primaryGenreName,
            country = track.country,
            artworkUrl = track.artworkUrl100.replaceAfterLast('/', "512x512bb.jpg"),
            timerText = defaultTimerText,
            playerState = PlayerState.PREPARED
        )
    }

    fun play() {
        handler.removeCallbacks(updateTimerRunnable)
        audioPlayer.play()
        currentTrack?.let { history.addTrackToHistory(it) }
        updateScreenState(playerState = PlayerState.PLAYING)
        handler.post(updateTimerRunnable)
    }

    fun pause() {
        handler.removeCallbacks(updateTimerRunnable)
        audioPlayer.pause()
        updateScreenState(playerState = PlayerState.PAUSED)
    }

    fun togglePlayPause() {
        when (_screenState.value?.playerState) {
            PlayerState.PLAYING -> pause()
            PlayerState.PREPARED, PlayerState.PAUSED -> play()
            else -> { /* не обрабатываем другие состояния */ }
        }
    }

    private fun updateScreenState(
        timerText: String? = null,
        playerState: PlayerState? = null
    ) {
        // Обновляем только те поля, которые изменились, оставляя остальные неизменными
        val current = _screenState.value ?: return
        _screenState.value = current.copy(
            timerText = timerText ?: current.timerText,
            playerState = playerState ?: current.playerState
        )
    }

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
