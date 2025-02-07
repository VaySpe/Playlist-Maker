package com.example.playlistmaker.data.repository

import android.icu.text.SimpleDateFormat
import android.util.Log
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
            Log.d("TracksRepositoryImpl", "Raw trackTimeMillis from API: ${trackDto.trackTimeMillis}")

            val convertedTime = convertTrackTime(trackDto.trackTimeMillis)
            Log.d("TracksRepositoryImpl", "Converted trackTime for UI: $convertedTime")

            Track(
                trackName = trackDto.trackName.orEmpty(),
                artistName = trackDto.artistName.orEmpty(),
                trackTime = convertedTime,
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
        Log.d("TracksRepositoryImpl", "Converting trackTimeMillis: $timeMillis")

        return if (timeMillis != null && timeMillis > 0) {
            val formattedTime = String.format("%02d:%02d", (timeMillis / 60000), (timeMillis / 1000) % 60)
            Log.d("TracksRepositoryImpl", "Formatted trackTime: $formattedTime")
            formattedTime
        } else {
            Log.e("TracksRepositoryImpl", "Error: trackTimeMillis is null or 0!")
            "00:00"
        }
    }
}
