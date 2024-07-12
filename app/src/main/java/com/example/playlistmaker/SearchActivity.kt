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
    private var searchLine: String = AMOUNT_DEF
    private lateinit var inputEditText: EditText
    private lateinit var clearButton: ImageView
    private lateinit var queryInput: EditText
    private lateinit var trackAdapter: TrackAdapter
    private val tracks = ArrayList<Track>()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var currentCall: Call<TrackResponse>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        inputEditText = findViewById(R.id.inputEditText)
        clearButton = findViewById(R.id.clearIcon)
        val recycler = findViewById<RecyclerView>(R.id.tracksList)
        var noResultsView = findViewById<LinearLayout>(R.id.no_results_view)
        var errorView = findViewById<LinearLayout>(R.id.error_view)
        var retryButton = findViewById<Button>(R.id.retry_button)

        fun hideAllViews() {
            recycler.isVisible = false
            noResultsView.isVisible = false
            errorView.isVisible = false
        }

        val backSearchBtn = findViewById<androidx.appcompat.widget.Toolbar>(R.id.search_back)

        backSearchBtn.setOnClickListener {
            finish()
        }

        inputEditText.setText(searchLine)

        clearButton.setOnClickListener {
            tracks.clear()
            trackAdapter.notifyDataSetChanged()
            inputEditText.setText("")
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(inputEditText.windowToken, 0)
        }

        val trackBaseUrl = getString(R.string.base_url)

        val retrofit = Retrofit.Builder()
            .baseUrl(trackBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val trackService = retrofit.create(iTunesApi::class.java)

        fun showToast(message: String) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }

        fun getTrack(text: String) {
            Log.d("TRACK_LOG", "Starting search for: $text")
            hideAllViews()

            val timeoutRunnable = Runnable {
                if (!::currentCall.isInitialized || !currentCall.isExecuted) return@Runnable
                currentCall.cancel()
                errorView.isVisible = true
            }
            handler.postDelayed(timeoutRunnable, 10000) // 10 секунд

            currentCall = trackService.search(text)
            currentCall.enqueue(object : Callback<TrackResponse> {
                override fun onResponse(
                    call: Call<TrackResponse>,
                    response: Response<TrackResponse>
                ) {
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
                                    artworkUrl100 = content.artworkUrl100
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
                        Log.e(
                            "TRACK_LOG",
                            "Response not successful: ${response.errorBody()?.string()}"
                        )
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

        queryInput = findViewById(R.id.inputEditText)
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
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(PRODUCT_AMOUNT, searchLine)
        Log.d("SearchActivity", "onSaveInstanceState: $searchLine")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchLine = savedInstanceState.getString(PRODUCT_AMOUNT, AMOUNT_DEF)
        inputEditText.setText(searchLine)
        Log.d("SearchActivity", "onRestoreInstanceState: $searchLine")
    }

    companion object {
        const val PRODUCT_AMOUNT = "PRODUCT_AMOUNT"
        const val AMOUNT_DEF = ""
    }
}
