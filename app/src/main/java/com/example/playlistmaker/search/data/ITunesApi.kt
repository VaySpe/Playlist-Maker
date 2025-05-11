package com.example.playlistmaker.search.data

import retrofit2.http.GET
import retrofit2.http.Query

interface ITunesApi {
    @GET("search?entity=song")
    suspend fun search(@Query("term") text: String): TrackResponseDto
}