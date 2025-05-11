package com.example.playlistmaker.search.domain

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
