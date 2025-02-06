package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repository.TracksRepository

class SearchTracksUseCase(
    private val repository: TracksRepository
) {
    suspend fun execute(query: String): List<Track> {
        // Можно добавить дополнительную валидацию, например,
        // если query пустой — возвращаем пустой список
        if (query.isBlank()) return emptyList()
        return repository.searchTracks(query)
    }
}
