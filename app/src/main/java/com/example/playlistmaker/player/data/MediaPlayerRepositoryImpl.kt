package com.example.playlistmaker.player.data

import android.content.Context
import android.media.MediaPlayer
import com.example.playlistmaker.player.domain.PlayerState
import com.example.playlistmaker.presentation.library.PlayerRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MediaPlayerRepositoryImpl(
    private val context: Context
) : PlayerRepository {

    private var mediaPlayer: MediaPlayer? = null
    private var playerState: PlayerState = PlayerState.DEFAULT
    private var onCompletionListener: (() -> Unit)? = null // Храним колбэк

    override fun setOnCompletionListener(listener: () -> Unit) {
        onCompletionListener = listener
    }

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
