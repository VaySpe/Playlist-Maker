package com.example.playlistmaker.search.ui

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.search.domain.HistoryInteract
import com.example.playlistmaker.search.domain.SearchTracksUseCase
import com.example.playlistmaker.search.domain.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchTracksUseCase: SearchTracksUseCase,
    private val historyUseCase: HistoryInteract
) : ViewModel() {

    companion object {
        private const val DEBOUNCE_DELAY = 2000L
    }

    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private var searchJob: Job? = null

    // LiveData состояния UI
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _searchResults = MutableLiveData<List<Track>>()
    val searchResults: LiveData<List<Track>> = _searchResults

    private val _showNoResults = MutableLiveData<Boolean>()
    val showNoResults: LiveData<Boolean> = _showNoResults

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _history = MutableLiveData<List<Track>>()
    val history: LiveData<List<Track>> = _history

    private val _showClearButton = MutableLiveData<Boolean>()
    val showClearButton: LiveData<Boolean> = _showClearButton

    private val _navigateToPlayer = MutableLiveData<Track?>()
    val navigateToPlayer: LiveData<Track?> = _navigateToPlayer

    fun onQueryChanged(query: String) {
        _showClearButton.value = query.isNotEmpty()

        if (query.isEmpty()) {
            _searchResults.value = emptyList()
            _showNoResults.value = false
            _errorMessage.value = null
            _history.value = historyUseCase.getHistory()
            return
        }

        // Очистка предыдущего debounce
        searchRunnable?.let { handler.removeCallbacks(it) }

        searchRunnable = Runnable {
            search(query)
        }
        handler.postDelayed(searchRunnable!!, DEBOUNCE_DELAY)
    }

    private fun search(query: String) {
        _isLoading.value = true
        _errorMessage.value = null
        _showNoResults.value = false
        _searchResults.value = emptyList()

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                val results = searchTracksUseCase.execute(query)
                _searchResults.value = results
                _showNoResults.value = results.isEmpty()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onTrackClicked(track: Track) {
        historyUseCase.addTrackToHistory(track)
        _navigateToPlayer.value = track
    }

    fun onNavigationHandled() {
        _navigateToPlayer.value = null
    }

    fun onClearHistoryClicked() {
        historyUseCase.clearHistory()
        _history.value = emptyList()
    }

    fun loadHistoryIfEmptyQuery(currentQuery: String) {
        if (currentQuery.isEmpty()) {
            _history.value = historyUseCase.getHistory()
        }
    }
}
