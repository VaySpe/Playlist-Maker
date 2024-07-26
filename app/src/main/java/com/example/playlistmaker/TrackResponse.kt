package com.example.playlistmaker

data class TrackResponse(
    val resultCount: Int,
    val results: List<Content>
)

data class Content(
    val trackName: String,
    val artistName: String,
    val trackTimeMillis: Int,
    val artworkUrl100: String,
    val trackId: Int,
    val collectionName: String,
    val releaseDate: String,
    val primaryGenreName: String,
    val country: String
)