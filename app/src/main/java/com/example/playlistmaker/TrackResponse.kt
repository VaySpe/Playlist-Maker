package com.example.playlistmaker

class TrackResponse(val resultCount: Int,
                    val results: List<Content>)

data class Content(val trackName: String,
    val artistName: String,
    val trackTimeMillis: Int,
    val artworkUrl100: String)