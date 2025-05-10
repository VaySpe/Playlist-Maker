package com.example.playlistmaker.search.data

import android.content.Context
import android.content.SharedPreferences
import com.example.playlistmaker.search.domain.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedPrefsLocalStorage(
    context: Context,
    private val gson: Gson
) : LocalStorage {

    companion object {
        private const val PREFERENCES_NAME = "playlist_maker_prefs"
        private const val HISTORY_KEY = "key_for_history_tracks"
    }

    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun loadHistory(): List<Track> {
        val json = sharedPrefs.getString(HISTORY_KEY, null) ?: return emptyList()
        val type = object : TypeToken<List<Track>>() {}.type
        return gson.fromJson(json, type)
    }

    override fun saveHistory(history: List<Track>) {
        val json = gson.toJson(history)
        sharedPrefs.edit()
            .putString(HISTORY_KEY, json)
            .apply()
    }
}
