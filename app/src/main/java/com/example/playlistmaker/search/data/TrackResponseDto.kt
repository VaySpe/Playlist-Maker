package com.example.playlistmaker.search.data

data class TrackResponseDto(
    val resultCount: Int,
    val results: List<TrackDto>
)