package com.example.playlistmaker.data.dto

data class TrackResponseDto(
    val resultCount: Int,
    val results: List<TrackDto>
)