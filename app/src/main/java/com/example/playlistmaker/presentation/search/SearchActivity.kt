package com.example.playlistmaker.presentation.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.player.ui.AudioplayerActivity
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.search.adapter.TrackAdapter
import com.example.playlistmaker.domain.models.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private var isClickAllowed = true

    // Получаем UseCase из Creator
    private val searchTracksUseCase = Creator.provideSearchTracksUseCase()
    private val historyUseCase = Creator.provideHistoryUseCase()

    // Views
    private lateinit var inputEditText: EditText
    private lateinit var clearButton: ImageView
    private lateinit var clearHistoryButton: Button
    private lateinit var recycler: RecyclerView
    private lateinit var historyRecycler: RecyclerView
    private lateinit var noResultsView: LinearLayout
    private lateinit var errorView: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var historyView: LinearLayout
    private lateinit var retryButton: Button

    private lateinit var trackAdapter: TrackAdapter
    private lateinit var historyTrackAdapter: TrackAdapter

    private val tracks = ArrayList<Track>()
    private val SEARCH_DEBOUNCE_DELAY = 2000L
    private val CLICK_DEBOUNCE_DELAY = 1000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Инициализируем Creator с AppContext, если не сделали это в Application
        // Creator.init(application)

        // Инициализируем View
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

        val backSearchBtn = findViewById<Toolbar>(R.id.search_back)
        backSearchBtn.setOnClickListener {
            finish()
        }

        // Set up adapters
        recycler.layoutManager = LinearLayoutManager(this)
        trackAdapter = TrackAdapter(tracks) { track ->
            onTrackClick(track)
        }
        recycler.adapter = trackAdapter

        historyRecycler.layoutManager = LinearLayoutManager(this)
        historyTrackAdapter = TrackAdapter(historyUseCase.getHistory().toMutableList()) { track ->
            onTrackClick(track)
        }
        historyRecycler.adapter = historyTrackAdapter

        // Кнопка очистки запроса
        clearButton.setOnClickListener {
            inputEditText.setText("")
            tracks.clear()
            trackAdapter.notifyDataSetChanged()
            hideAllViews()
            updateHistoryView()
            hideKeyboard()
        }

        // Кнопка очистки истории
        clearHistoryButton.setOnClickListener {
            historyUseCase.clearHistory()
            historyTrackAdapter.setData(emptyList())
            showToast("History cleared")
            updateHistoryView()
        }

        // Retry
        retryButton.setOnClickListener {
            performSearch(inputEditText.text.toString())
        }

        // Debounce для поиска
        inputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearButton.isVisible = !s.isNullOrEmpty()
                searchRunnable?.let { handler.removeCallbacks(it) }

                if (s.isNullOrEmpty()) {
                    hideAllViews()
                    updateHistoryView()
                    progressBar.isVisible = false
                } else {
                    // Скрываем историю и кнопку очистки истории
                    historyView.isVisible = false
                    clearHistoryButton.isVisible = false
                    historyRecycler.isVisible = false

                    // Debounce для поиска
                    searchRunnable = Runnable {
                        performSearch(s.toString())
                    }
                    handler.postDelayed(searchRunnable!!, SEARCH_DEBOUNCE_DELAY)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        updateHistoryView()
        hideKeyboardOnDoneAction()
    }

    // Выполняем поиск
    private fun performSearch(query: String) {
        if (query.isBlank()) return
        showLoading(true)
        hideAllViews()

        // Запускаем корутину
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = searchTracksUseCase.execute(query)
                showLoading(false)
                if (result.isEmpty()) {
                    showNoResults()
                } else {
                    showTracks(result)
                }
            } catch (e: Exception) {
                showLoading(false)
                showError(e.message.orEmpty())
            }
        }
    }

    private fun onTrackClick(track: Track) {
        if (!clickDebounce()) return
        // Добавляем в историю
        historyUseCase.addTrackToHistory(track)
        // Обновляем адаптер истории
        historyTrackAdapter.setData(historyUseCase.getHistory())
        // Открываем плеер
        navigateToPlayer(track)
    }

    private fun hideAllViews() {
        recycler.isVisible = false
        noResultsView.isVisible = false
        errorView.isVisible = false
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.isVisible = isLoading
    }

    private fun showTracks(tracksList: List<Track>) {
        tracks.clear()
        tracks.addAll(tracksList)
        trackAdapter.notifyDataSetChanged()
        recycler.isVisible = true
    }

    private fun showNoResults() {
        noResultsView.isVisible = true
    }

    private fun showError(message: String) {
        errorView.isVisible = true
        showToast("Error: $message")
    }

    private fun updateHistoryView() {
        val history = historyUseCase.getHistory()
        val shouldShowHistory = history.isNotEmpty() && inputEditText.text.isEmpty()

        historyView.isVisible = shouldShowHistory
        clearHistoryButton.isVisible = shouldShowHistory
        historyRecycler.isVisible = shouldShowHistory

        if (shouldShowHistory) {
            historyTrackAdapter.setData(history)
        }
    }

    private fun navigateToPlayer(track: Track) {
        val trackJson = Creator.gson.toJson(track)
        val intent = Intent(this, AudioplayerActivity::class.java)
        intent.putExtra("track_json", trackJson)
        startActivity(intent)
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    private fun hideKeyboardOnDoneAction() {
        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard()
                true
            } else {
                false
            }
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(inputEditText.windowToken, 0)
    }
}
