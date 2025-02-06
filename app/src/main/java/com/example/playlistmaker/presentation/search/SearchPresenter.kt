package com.example.playlistmaker.presentation.search

import android.os.Handler
import android.os.Looper
import com.example.playlistmaker.domain.usecase.HistoryUseCase
import com.example.playlistmaker.domain.usecase.SearchTracksUseCase
import com.example.playlistmaker.domain.models.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchPresenter(
    private val searchUseCase: SearchTracksUseCase,
    private val historyUseCase: HistoryUseCase
) {
    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private var searchJob: Job? = null

    var view: SearchView? = null

    fun attachView(view: SearchView) {
        this.view = view
    }

    fun detachView() {
        this.view = null
        // Прерываем корутину, если она запущена
        searchJob?.cancel()
        // Снимаем запланированные операции поиска (debounce)
        searchRunnable?.let { handler.removeCallbacks(it) }
    }

    /**
     * Вызывается при изменении текста в поисковой строке
     */
    fun onQueryChanged(query: String) {
        // Отображаем кнопку очистки, если строка не пустая
        view?.showClearButton(query.isNotEmpty())

        // Если строка поиска пуста — показываем историю, иначе скрываем
        if (query.isEmpty()) {
            val historyList = historyUseCase.getHistory()
            view?.showHistory(historyList)
        } else {
            view?.hideHistory()
        }

        // Настраиваем Debounce: сбрасываем предыдущий Runnable
        searchRunnable?.let { handler.removeCallbacks(it) }

        // Если запрос непустой, запуск поиска с задержкой
        if (query.isNotEmpty()) {
            searchRunnable = Runnable {
                searchTracks(query)
            }
            handler.postDelayed(searchRunnable!!, DEBOUNCE_DELAY)
        }
    }

    /**
     * Непосредственно запускает поиск треков и обновляет UI через View
     */
    private fun searchTracks(query: String) {
        view?.showLoading(true)
        view?.hideError()
        view?.hideNoResults()
        view?.hideTracks()

        // Запускаем корутину в IO-потоке для выполнения сетевого/долгой операции
        searchJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                // Выполняем бизнес-логику поиска
                val result = searchUseCase.execute(query)

                withContext(Dispatchers.Main) {
                    view?.showLoading(false)
                    if (result.isEmpty()) {
                        view?.showNoResults()
                    } else {
                        view?.showTracks(result)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view?.showLoading(false)
                    view?.showError(e.message.orEmpty())
                }
            }
        }
    }

    /**
     * Вызывается при клике на конкретный трек
     */
    fun onTrackClicked(track: Track) {
        // Добавляем трек в историю
        historyUseCase.addTrackToHistory(track)
        // Открываем плеер
        view?.navigateToPlayer(track)
    }

    /**
     * Вызывается при клике на кнопку \"Очистить историю\"
     */
    fun onClearHistoryClicked() {
        historyUseCase.clearHistory()
        view?.showHistory(emptyList())
    }

    companion object {
        private const val DEBOUNCE_DELAY = 2000L
    }
}
