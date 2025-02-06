package com.example.playlistmaker.data.repository

import android.content.Context
import android.media.MediaPlayer
import androidx.core.content.ContextCompat
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.PlayerState
import com.example.playlistmaker.domain.repository.PlayerRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MediaPlayerRepositoryImpl(
    private val context: Context
) : PlayerRepository {

    private var mediaPlayer: MediaPlayer? = null
    private var playerState: PlayerState = PlayerState.DEFAULT

    override suspend fun prepare(previewUrl: String) {
        release() // очищаем предыдущий плеер, если был
        mediaPlayer = MediaPlayer().apply {
            setDataSource(previewUrl)
            setOnPreparedListener {
                playerState = PlayerState.PREPARED
            }
            setOnCompletionListener {
                playerState = PlayerState.PREPARED
                // Можно сбросить позицию, если нужно
            }
            prepareAsync()
        }

        // Ждём, пока onPreparedListener сработает
        suspendCancellableCoroutine<Unit> { continuation ->
            mediaPlayer?.setOnPreparedListener {
                playerState = PlayerState.PREPARED
                continuation.resume(Unit)
            }
            mediaPlayer?.setOnErrorListener { _, what, extra ->
                continuation.resumeWithException(Exception("MediaPlayer error: $what $extra"))
                true
            }
        }
    }

    override fun play() {
        if (playerState == PlayerState.PREPARED || playerState == PlayerState.PAUSED) {
            mediaPlayer?.start()
            playerState = PlayerState.PLAYING
        }
    }

    override fun pause() {
        if (playerState == PlayerState.PLAYING) {
            mediaPlayer?.pause()
            playerState = PlayerState.PAUSED
        }
    }

    override fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        playerState = PlayerState.DEFAULT
    }

    override fun getCurrentPositionMs(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    override fun getState(): PlayerState {
        return playerState
    }
}
