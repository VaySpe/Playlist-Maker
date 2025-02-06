package com.example.playlistmaker.data.repository

import android.icu.text.SimpleDateFormat
import com.example.playlistmaker.data.storage.LocalStorage
import com.example.playlistmaker.data.network.ITunesApi
import com.example.playlistmaker.domain.repository.TracksRepository
import com.example.playlistmaker.domain.models.Track
import java.util.Locale

class TracksRepositoryImpl(
    private val iTunesApi: ITunesApi,
    private val localStorage: LocalStorage // условный интерфейс для SharedPreferences
) : TracksRepository {

    override suspend fun searchTracks(query: String): List<Track> {
        // 1) Вызываем сетевой метод
        val response = iTunesApi.search(query)

        // 2) Преобразуем DTO в Domain-модель
        return response.results.map { trackDto ->
            Track(
                trackName = trackDto.trackName.orEmpty(),
                artistName = trackDto.artistName.orEmpty(),
                trackTime = convertTrackTime(trackDto.trackTime?.toLong()),
                artworkUrl100 = trackDto.artworkUrl100.orEmpty(),
                trackId = trackDto.trackId.orEmpty(),
                collectionName = trackDto.collectionName.orEmpty(),
                releaseDate = trackDto.releaseDate.orEmpty(),
                primaryGenreName = trackDto.primaryGenreName.orEmpty(),
                country = trackDto.country.orEmpty(),
                previewUrl = trackDto.previewUrl.orEmpty()
            )
        }
    }

    override fun getHistory(): List<Track> {
        return localStorage.loadHistory()
    }

    override fun saveHistory(history: List<Track>) {
        localStorage.saveHistory(history)
    }

    private fun convertTrackTime(timeMillis: Long?): String {
        // Здесь можно применить SimpleDateFormat или возвращать просто Long
        // в зависимости от того, нужна ли строка в домене
        if (timeMillis == null) return ""
        // Пример форматирования
        val format = SimpleDateFormat("mm:ss", Locale.getDefault())
        return format.format(timeMillis)
    }
}
