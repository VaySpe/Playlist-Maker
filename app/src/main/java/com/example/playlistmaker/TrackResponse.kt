package com.example.playlistmaker

class TrackResponse(val contents: Content)  {
    data class Content(val resultCount: Int,
                       val results: List<ContentTrack>)

    data class ContentTrack(val trackName: String,
        val artistName: String,
        val trackTimeMillis: Int,
        val artworkUrl100: String
    )
}