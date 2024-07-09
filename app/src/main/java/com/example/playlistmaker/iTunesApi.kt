package com.example.playlistmaker

import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query

interface iTunesApi {
    @POST("search")
    fun search(@Query("term") text: String): Call<TrackResponse>
}