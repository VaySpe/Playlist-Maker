package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repository.TracksRepository

class HistoryImpl(
    private val repository: TracksRepository
): HistoryInteract {
    override fun getHistory(): List<Track> {
        return repository.getHistory()
    }

    override fun addTrackToHistory(track: Track) {
        val current = repository.getHistory().toMutableList()
        // 1. Удаляем, если трек уже есть
        current.removeAll { it.trackId == track.trackId }
        // 2. Добавляем в начало
        current.add(0, track)
        // 3. Ограничиваем
        if (current.size > MAX_HISTORY_TRACKS) {
            current.removeLast()
        }
        // 4. Сохраняем
        repository.saveHistory(current)
    }

    override fun clearHistory() {
        repository.saveHistory(emptyList())
    }

    companion object {
        private const val MAX_HISTORY_TRACKS = 10
    }
}
