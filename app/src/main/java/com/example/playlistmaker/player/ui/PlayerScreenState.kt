package com.example.playlistmaker.player.ui

import com.example.playlistmaker.player.domain.PlayerState

data class PlayerScreenState(
    val trackName: String,
    val artistName: String,
    val album: String,
    val duration: String,
    val year: String,
    val genre: String,
    val country: String,
    val artworkUrl: String,
    val timerText: String,
    val playerState: PlayerState
)
