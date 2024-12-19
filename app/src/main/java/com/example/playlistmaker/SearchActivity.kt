package com.example.playlistmaker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Locale

const val HISTORY_TRACKS_KEY = "key_for_history_tracks"
const val MAX_HISTORY_TRACKS = 10

class SearchActivity : AppCompatActivity(), TrackAdapter.OnTrackClickListener {

    private var searchLine: String = EMPTY
    private lateinit var inputEditText: EditText
    private lateinit var clearButton: ImageView
    private lateinit var clearHistoryButton: Button
    private lateinit var queryInput: EditText
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var historyTrackAdapter: TrackAdapter
    private lateinit var recycler: RecyclerView
    private lateinit var historyRecycler: RecyclerView
    private lateinit var noResultsView: LinearLayout
    private lateinit var errorView: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var historyView: LinearLayout
    private lateinit var retryButton: Button
    private val tracks = ArrayList<Track>()
    private val historyTracks = ArrayList<Track>()
    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private var isClickAllowed = true
    private lateinit var currentCall: Call<TrackResponse>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        inputEditText = findViewById(R.id.inputEditText)
        clearButton = findViewById(R.id.clearIcon)
        clearHistoryButton = findViewById(R.id.clear_history_button)
        recycler = findViewById(R.id.tracksList)
        historyRecycler = findViewById(R.id.tracksListHistory)
        noResultsView = findViewById(R.id.no_results_view)
        errorView = findViewById(R.id.error_view)
        progressBar = findViewById(R.id.progressBar)
        retryButton = findViewById(R.id.retry_button)
        historyView = findViewById(R.id.history_view)
        queryInput = findViewById(R.id.inputEditText)

        val backSearchBtn = findViewById<androidx.appcompat.widget.Toolbar>(R.id.search_back)

        backSearchBtn.setOnClickListener {
            finish()
        }

        inputEditText.setText(searchLine)

        clearButton.setOnClickListener {
            tracks.clear()
            trackAdapter.notifyDataSetChanged()
            inputEditText.setText(EMPTY)
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(inputEditText.windowToken, 0)
            hideAllViews()
            updateHistoryView()
        }

        clearHistoryButton.setOnClickListener {
            clearHistoryTracks()
        }

        hideKeyboard(findViewById(R.id.inputEditText))

        retryButton.setOnClickListener {
            getTrack(queryInput.text.toString())
        }

        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // empty
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchLine = s.toString()
                clearButton.isVisible = searchLine.isNotEmpty()
                recycler.isVisible = searchLine.isNotEmpty()
                updateHistoryView()
            }

            override fun afterTextChanged(s: Editable?) {
                searchRunnable?.let { handler.removeCallbacks(it)}

                if(s.isNullOrEmpty()){
                    hideAllViews()
                    updateHistoryView()
                    progressBar.isVisible = false
                    return
                }

                searchRunnable = Runnable{
                    performSearch(s.toString())
                }
                handler.postDelayed(searchRunnable!!, SEARCH_DEBOUNCE_DELAY)
            }
        }

        inputEditText.addTextChangedListener(simpleTextWatcher)

        recycler.layoutManager = LinearLayoutManager(this)
        trackAdapter = TrackAdapter(tracks, this)
        recycler.adapter = trackAdapter

        val sharedPrefs = getSharedPreferences(PLAYLIST_MAKER_PREFERENCES, MODE_PRIVATE)
        val jsonHistoryTracks = sharedPrefs.getString(HISTORY_TRACKS_KEY, "")

        if (!jsonHistoryTracks.isNullOrEmpty()) {
            val gson = Gson()
            val type = object : TypeToken<ArrayList<Track>>() {}.type
            val savedTracks: ArrayList<Track> = gson.fromJson(jsonHistoryTracks, type)
            historyTracks.addAll(savedTracks)
        }

        historyRecycler.layoutManager = LinearLayoutManager(this)
        historyTrackAdapter = TrackAdapter(historyTracks, this)
        historyRecycler.adapter = historyTrackAdapter
        loadHistoryTracks()

        updateHistoryView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_QUERY_KEY, searchLine)
        Log.d("SearchActivity", "onSaveInstanceState: $searchLine")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchLine = savedInstanceState.getString(SEARCH_QUERY_KEY, EMPTY)
        inputEditText.setText(searchLine)
        Log.d("SearchActivity", "onRestoreInstanceState: $searchLine")
    }

    companion object {
        const val SEARCH_QUERY_KEY = "SEARCH_QUERY_KEY"
        const val EMPTY = ""
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }

    private fun hideAllViews() {
        recycler.isVisible = false
        noResultsView.isVisible = false
        errorView.isVisible = false
    }

    private fun trackService(): iTunesApi {
        val trackBaseUrl = getString(R.string.base_url)

        val retrofit = Retrofit.Builder()
            .baseUrl(trackBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(iTunesApi::class.java)
    }

    private fun performSearch(searchRequest: String) {
        if (searchRequest.isEmpty()) {
            progressBar.isVisible = false
            return
        }

        progressBar.isVisible = true
        getTrack(searchRequest)
    }

    private fun hideKeyboard(queryInput: EditText) {
        queryInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val inputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                inputMethodManager?.hideSoftInputFromWindow(inputEditText.windowToken, 0)
                true
            } else {
                false
            }
        }
    }

    private fun getTrack(text: String) {
        Log.d("TRACK_LOG", "Starting search for: $text")
        hideAllViews()

        val timeoutRunnable = Runnable {
            if (::currentCall.isInitialized && currentCall.isExecuted) {
                currentCall.cancel()
                errorView
                showToast(getString(R.string.request_timed_out))
            }
        }
        handler.postDelayed(timeoutRunnable, 10000) // 10 секунд

        currentCall = trackService().search(text)
        currentCall.enqueue(object : Callback<TrackResponse> {
            override fun onResponse(call: Call<TrackResponse>, response: Response<TrackResponse>) {
                handler.removeCallbacks(timeoutRunnable)
                Log.d("TRACK_LOG", "Response code: ${response.code()}")
                progressBar.isVisible = false
                if (response.isSuccessful) {
                    val trackResponse = response.body()
                    Log.d("TRACK_LOG", "Raw response: ${response.raw()}")
                    if (trackResponse != null && trackResponse.results.isNotEmpty()) {
                        val formattedTracks = trackResponse.results.map { content ->
                            Track(
                                trackName = content.trackName,
                                artistName = content.artistName,
                                trackTime = SimpleDateFormat(
                                    "mm:ss",
                                    Locale.getDefault()
                                ).format(content.trackTimeMillis),
                                artworkUrl100 = content.artworkUrl100,
                                trackId = content.trackId,
                                collectionName = content.collectionName,
                                releaseDate = content.releaseDate,
                                primaryGenreName = content.primaryGenreName,
                                country = content.country,
                                previewUrl = content.previewUrl
                            )
                        }
                        tracks.clear()
                        tracks.addAll(formattedTracks)
                        trackAdapter.notifyDataSetChanged()
                        recycler.isVisible = true
                    } else {
                        noResultsView.isVisible = true
                    }
                    Log.d("TRACK_LOG", "Tracks: ${trackResponse?.results}")
                } else {
                    Log.e("TRACK_LOG", "Response not successful: ${response.errorBody()?.string()}")
                    errorView.isVisible = true
                    showToast("Код ошибки: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<TrackResponse>, t: Throwable) {
                handler.removeCallbacks(timeoutRunnable)
                Log.e("TRACK_LOG", "Error fetching tracks", t)
                recycler.isVisible = false
                errorView.isVisible = true
            }
        })
    }

    private fun addTrackToHistory(newTrack: Track) {
        val sharedPrefs = getSharedPreferences(PLAYLIST_MAKER_PREFERENCES, MODE_PRIVATE)
        val gson = Gson()
        loadHistoryTracks()

        // Удаляем трек из истории, если он уже существует
        historyTracks.removeAll { it.trackId == newTrack.trackId }

        // Добавляем новый трек в начало списка истории
        historyTracks.add(0, newTrack)

        // Ограничиваем размер списка истории
        if (historyTracks.size > MAX_HISTORY_TRACKS) {
            historyTracks.removeAt(historyTracks.size - 1)
        }

        // Сохраните обновленную историю
        val editor = sharedPrefs.edit()
        val updatedJsonHistoryTracks = gson.toJson(historyTracks)
        editor.putString(HISTORY_TRACKS_KEY, updatedJsonHistoryTracks)
        editor.apply()

        updateHistoryView()
    }

    private fun loadHistoryTracks() {
        val sharedPrefs = getSharedPreferences(PLAYLIST_MAKER_PREFERENCES, MODE_PRIVATE)
        val jsonHistoryTracks = sharedPrefs.getString(HISTORY_TRACKS_KEY, "")
        if (!jsonHistoryTracks.isNullOrEmpty()) {
            val gson = Gson()
            val type = object : TypeToken<ArrayList<Track>>() {}.type
            val savedTracks: ArrayList<Track> = gson.fromJson(jsonHistoryTracks, type)
            historyTracks.clear()
            historyTracks.addAll(savedTracks)
        }
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onTrackClick(track: Track) {
        if (clickDebounce()) {
            trackAdapter.selectedTrack = track
            addTrackToHistory(track)
            historyTrackAdapter.notifyDataSetChanged()

            val gson = Gson()
            val trackJson = gson.toJson(track)

            val intent = Intent(this, AudioplayerActivity::class.java)
            intent.putExtra("track_json", trackJson)
            startActivity(intent)
        }
    }

    private fun clearHistoryTracks() {
        historyTracks.clear()
        historyTrackAdapter.notifyDataSetChanged()

        val sharedPrefs = getSharedPreferences(PLAYLIST_MAKER_PREFERENCES, MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        editor.remove(HISTORY_TRACKS_KEY)
        editor.apply()

        showToast("History cleared")
        updateHistoryView()
    }

    private fun updateHistoryView() {
        historyView.isVisible = historyTracks.isNotEmpty() && searchLine.isEmpty()
    }

    private fun clickDebounce() : Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }
}
