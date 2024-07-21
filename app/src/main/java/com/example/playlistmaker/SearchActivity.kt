package com.example.playlistmaker

import android.content.Context
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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Locale

class SearchActivity : AppCompatActivity() {
    private var searchLine: String = EMPTY
    private lateinit var inputEditText: EditText
    private lateinit var clearButton: ImageView
    private lateinit var queryInput: EditText
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var historyTrackAdapter: HistoryTrackAdapter
    private lateinit var recycler: RecyclerView
    private lateinit var historyRecycler: RecyclerView
    private lateinit var noResultsView: LinearLayout
    private lateinit var errorView: LinearLayout
    private lateinit var retryButton: Button
    private val tracks = ArrayList<Track>()
    private val historyTracks = ArrayList<Track>()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var currentCall: Call<TrackResponse>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        inputEditText = findViewById(R.id.inputEditText)
        clearButton = findViewById(R.id.clearIcon)
        recycler = findViewById(R.id.tracksList)
        historyRecycler = findViewById(R.id.tracksListHistory)
        noResultsView = findViewById(R.id.no_results_view)
        errorView = findViewById(R.id.error_view)
        retryButton = findViewById(R.id.retry_button)

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
        }

        setupSearchActionListener(findViewById(R.id.inputEditText))

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
            }

            override fun afterTextChanged(s: Editable?) {
                // empty
            }
        }

        inputEditText.addTextChangedListener(simpleTextWatcher)

        recycler.layoutManager = LinearLayoutManager(this)
        trackAdapter = TrackAdapter(tracks)
        recycler.adapter = trackAdapter

        historyRecycler.layoutManager = LinearLayoutManager(this)
        historyTrackAdapter = HistoryTrackAdapter(historyTracks)
        historyRecycler.adapter = historyTrackAdapter
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

    private fun setupSearchActionListener(queryInput: EditText) {
        queryInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                getTrack(queryInput.text.toString())
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
                errorView.isVisible = true
                showToast(getString(R.string.request_timed_out))
            }
        }
        handler.postDelayed(timeoutRunnable, 10000) // 10 секунд

        currentCall = trackService().search(text)
        currentCall.enqueue(object : Callback<TrackResponse> {
            override fun onResponse(call: Call<TrackResponse>, response: Response<TrackResponse>) {
                handler.removeCallbacks(timeoutRunnable)
                Log.d("TRACK_LOG", "Response code: ${response.code()}")
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
                                trackId = content.trackId
                            )
                        }
                        tracks.clear()
                        tracks.addAll(formattedTracks)
                        historyTracks.add(formattedTracks[0]) // добавлять по клику сохранять и доставать
                        historyTrackAdapter.notifyDataSetChanged()
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

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
