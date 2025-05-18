package com.example.playlistmaker.search.ui

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.*
import com.example.playlistmaker.search.domain.HistoryInteract
import com.example.playlistmaker.search.domain.SearchTracksUseCase
import com.example.playlistmaker.search.domain.Track
import kotlinx.coroutines.Job
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

    private val _screenState = MutableLiveData(SearchScreenState())
    val screenState: LiveData<SearchScreenState> = _screenState

    fun onQueryChanged(query: String) {
        updateState { it.copy(query = query, errorMessage = null, isLoading = false, navigateTo = null) }

        searchRunnable?.let { handler.removeCallbacks(it) }
        searchRunnable = null
        searchJob?.cancel()

        if (query.isEmpty()) {
            updateState {
                it.copy(
                    searchResults = emptyList(),
                    errorMessage = null,
                    history = historyUseCase.getHistory()
                )
            }
            return
        }

        searchRunnable = Runnable { search(query) }
        handler.postDelayed(searchRunnable!!, DEBOUNCE_DELAY)
    }

    private fun search(query: String) {
        updateState { it.copy(isLoading = true, searchResults = emptyList(), errorMessage = null, history = emptyList()) }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                val results = searchTracksUseCase.execute(query)
                updateState {
                    it.copy(
                        isLoading = false,
                        searchResults = results,
                        errorMessage = if (results.isEmpty()) "Ничего не найдено" else null
                    )
                }
            } catch (e: Exception) {
                updateState {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Неизвестная ошибка",
                        searchResults = emptyList()
                    )
                }
            }
        }
    }

    fun onTrackClicked(track: Track) {
        historyUseCase.addTrackToHistory(track)
        updateState { it.copy(navigateTo = track) }
    }

    fun onNavigationHandled() {
        updateState { it.copy(navigateTo = null) }
    }

    fun onClearHistoryClicked() {
        historyUseCase.clearHistory()
        updateState { it.copy(history = emptyList()) }
    }

    fun loadHistoryIfEmptyQuery() {
        val current = _screenState.value ?: return
        if (current.query.isEmpty()) {
            updateState { it.copy(history = historyUseCase.getHistory()) }
        }
    }

    private fun updateState(update: (SearchScreenState) -> SearchScreenState) {
        val current = _screenState.value ?: SearchScreenState()
        _screenState.value = update(current)
    }
}
