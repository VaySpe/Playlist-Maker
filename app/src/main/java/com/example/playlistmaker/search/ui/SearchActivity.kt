package com.example.playlistmaker.search.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.player.ui.AudioplayerActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.android.ext.android.inject
import com.google.gson.Gson

class SearchActivity : AppCompatActivity() {

    private val viewModel: SearchViewModel by viewModel()
    private val gson: Gson by inject()

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        bindViews()
        setupRecyclerViews()
        setupListeners()
        observeScreenState()

        viewModel.loadHistoryIfEmptyQuery()
        hideKeyboardOnDoneAction()
    }

    private fun bindViews() {
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

        findViewById<Toolbar>(R.id.search_back).setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerViews() {
        recycler.layoutManager = LinearLayoutManager(this)
        trackAdapter = TrackAdapter(mutableListOf()) { track ->
            viewModel.onTrackClicked(track)
        }
        recycler.adapter = trackAdapter

        historyRecycler.layoutManager = LinearLayoutManager(this)
        historyTrackAdapter = TrackAdapter(mutableListOf()) { track ->
            viewModel.onTrackClicked(track)
        }
        historyRecycler.adapter = historyTrackAdapter
    }

    private fun setupListeners() {
        clearButton.setOnClickListener {
            inputEditText.setText("")
            hideKeyboard()
            viewModel.onQueryChanged("")
        }

        clearHistoryButton.setOnClickListener {
            viewModel.onClearHistoryClicked()
        }

        retryButton.setOnClickListener {
            viewModel.onQueryChanged(inputEditText.text.toString())
        }

        inputEditText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onQueryChanged(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeScreenState() {
        viewModel.screenState.observe(this) { state ->

            progressBar.isVisible = state.isLoading
            recycler.isVisible = state.searchResults.isNotEmpty()
            trackAdapter.setData(state.searchResults)

            noResultsView.isVisible = state.errorMessage == "Ничего не найдено"
            errorView.isVisible = state.errorMessage != null && state.errorMessage != "Ничего не найдено"

            val showHistory = state.history.isNotEmpty() && state.query.isEmpty()
            historyView.isVisible = showHistory
            clearHistoryButton.isVisible = showHistory
            historyRecycler.isVisible = showHistory
            historyTrackAdapter.setData(state.history)

            clearButton.isVisible = state.query.isNotEmpty()

            state.navigateTo?.let {
                val intent = Intent(this, AudioplayerActivity::class.java)
                intent.putExtra("track_json", gson.toJson(it))
                startActivity(intent)
                viewModel.onNavigationHandled()
            }
        }
    }

    private fun hideKeyboardOnDoneAction() {
        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard()
                true
            } else false
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(inputEditText.windowToken, 0)
    }
}
