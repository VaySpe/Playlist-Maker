package com.example.playlistmaker.search.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.player.ui.AudioplayerActivity

class SearchActivity : AppCompatActivity() {

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

    private val viewModel: SearchViewModel by viewModels {
        Creator.provideSearchViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        bindViews()
        setupRecyclerViews()
        setupListeners()
        observeViewModel()

        viewModel.loadHistoryIfEmptyQuery("")
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

        findViewById<Toolbar>(R.id.search_back).setOnClickListener {
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
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onQueryChanged(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { loading ->
            progressBar.isVisible = loading

            if (loading) {
                // При загрузке скрываем всё остальное
                recycler.isVisible = false
                errorView.isVisible = false
                noResultsView.isVisible = false
                historyView.isVisible = false
            }
        }

        viewModel.searchResults.observe(this) { results ->
            recycler.isVisible = results.isNotEmpty()
            trackAdapter.setData(results)
        }

        viewModel.showNoResults.observe(this) { show ->
            noResultsView.isVisible = show
        }

        viewModel.errorMessage.observe(this) { error ->
            errorView.isVisible = !error.isNullOrEmpty()
            if (!error.isNullOrEmpty()) showToast("Ошибка: $error")
        }

        viewModel.history.observe(this) { history ->
            val shouldShow = history.isNotEmpty() && inputEditText.text.isEmpty()
            historyView.isVisible = shouldShow
            clearHistoryButton.isVisible = shouldShow
            historyRecycler.isVisible = shouldShow
            historyTrackAdapter.setData(history)
        }

        viewModel.showClearButton.observe(this) { show ->
            clearButton.isVisible = show
        }

        viewModel.navigateToPlayer.observe(this) { track ->
            track?.let {
                val trackJson = Creator.gson.toJson(it)
                val intent = Intent(this, AudioplayerActivity::class.java)
                intent.putExtra("track_json", trackJson)
                startActivity(intent)
                viewModel.onNavigationHandled()
            }
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
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
