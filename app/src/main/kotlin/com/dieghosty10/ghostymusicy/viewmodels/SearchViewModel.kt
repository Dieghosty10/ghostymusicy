package com.dieghosty10.ghostymusicy.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dieghosty10.ghostymusicy.db.MusicDatabase
import com.dieghosty10.ghostymusicy.innertube.YouTube
import com.dieghosty10.ghostymusicy.innertube.pages.SearchResult
import com.dieghosty10.ghostymusicy.innertube.models.SearchSuggestions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SearchTab(val label: String, val filter: YouTube.SearchFilter) {
    SONGS   ("Canciones", YouTube.SearchFilter.FILTER_SONG),
    ARTISTS ("Artistas",  YouTube.SearchFilter.FILTER_ARTIST),
    ALBUMS  ("Ãlbumes",   YouTube.SearchFilter.FILTER_ALBUM),
    VIDEOS  ("Videos",    YouTube.SearchFilter.FILTER_VIDEO),
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val database: MusicDatabase
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _suggestions = MutableStateFlow<SearchSuggestions?>(null)
    val suggestions: StateFlow<SearchSuggestions?> = _suggestions.asStateFlow()

    // Resultados por tab
    private val _results = MutableStateFlow<Map<SearchTab, SearchResult?>>(emptyMap())
    val results: StateFlow<Map<SearchTab, SearchResult?>> = _results.asStateFlow()

    // Tab activo
    private val _activeTab = MutableStateFlow(SearchTab.SONGS)
    val activeTab: StateFlow<SearchTab> = _activeTab.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    // Historial (en memoria, mÃ¡ximo 10)
    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var suggestJob: Job? = null
    private var searchJob: Job? = null

    fun setActiveTab(tab: SearchTab) {
        _activeTab.value = tab
        // Si ya tenemos query pero no resultados para este tab, buscar
        val q = _query.value
        if (q.isNotBlank() && _results.value[tab] == null) {
            loadTab(tab, q)
        }
    }

    fun updateQuery(newQuery: String) {
        _query.value = newQuery
        suggestJob?.cancel()
        if (newQuery.isBlank()) {
            _suggestions.value = null
            _results.value = emptyMap()
            return
        }
        suggestJob = viewModelScope.launch {
            delay(220)
            YouTube.searchSuggestions(newQuery).onSuccess { _suggestions.value = it }
        }
    }

    fun performSearch(queryText: String) {
        if (queryText.isBlank()) return
        suggestJob?.cancel()
        searchJob?.cancel()

        _query.value = queryText
        _suggestions.value = null
        _results.value = emptyMap()
        _error.value = null
        addToHistory(queryText)

        // Cargar el tab activo primero, los demÃ¡s en paralelo
        searchJob = viewModelScope.launch {
            _isLoading.value = true
            // Cargar tab activo
            loadTabSuspend(_activeTab.value, queryText)
            _isLoading.value = false
            // Cargar los demÃ¡s tabs en background
            SearchTab.entries.filter { it != _activeTab.value }.forEach { tab ->
                launch { loadTabSuspend(tab, queryText) }
            }
        }
    }

    private fun loadTab(tab: SearchTab, q: String) {
        viewModelScope.launch { loadTabSuspend(tab, q) }
    }

    private suspend fun loadTabSuspend(tab: SearchTab, q: String) {
        val result = YouTube.search(q, tab.filter)
        if (result.isSuccess) {
            _results.value = _results.value + (tab to result.getOrThrow())
        } else {
            val exception = result.exceptionOrNull()
            exception?.printStackTrace()
            _error.value = exception?.message ?: "Error al buscar"
        }
    }

    fun loadMore(tab: SearchTab) {
        if (_isLoadingMore.value) return
        val currentResult = _results.value[tab] ?: return
        val continuation = currentResult.continuation ?: return

        viewModelScope.launch {
            _isLoadingMore.value = true
            val newResult = YouTube.searchContinuation(continuation)
            if (newResult.isSuccess) {
                val nextData = newResult.getOrThrow()
                val mergedResult = SearchResult(
                    items = currentResult.items + nextData.items,
                    continuation = nextData.continuation
                )
                _results.value = _results.value + (tab to mergedResult)
            }
            _isLoadingMore.value = false
        }
    }

    private fun addToHistory(q: String) {
        val list = _recentSearches.value.toMutableList()
        list.remove(q)
        list.add(0, q)
        _recentSearches.value = list.take(10)
    }

    fun removeRecentSearch(q: String) {
        _recentSearches.value = _recentSearches.value.filter { it != q }
    }

    fun clearRecentSearches() {
        _recentSearches.value = emptyList()
    }

    fun saveToLibrary(item: com.dieghosty10.ghostymusicy.innertube.models.YTItem) {
        database.query {
            when (item) {
                is com.dieghosty10.ghostymusicy.innertube.models.SongItem -> {
                    insert(
                        com.dieghosty10.ghostymusicy.db.entities.SongEntity(
                            id = item.id,
                            title = item.title,
                            thumbnailUrl = item.thumbnail,
                            albumId = item.album?.id,
                            albumName = item.album?.name,
                            duration = item.duration ?: -1,
                            inLibrary = java.time.LocalDateTime.now()
                        )
                    )
                    // Insert artists if necessary, but skipping for brevity
                }
                is com.dieghosty10.ghostymusicy.innertube.models.AlbumItem -> { insert(com.dieghosty10.ghostymusicy.db.entities.AlbumEntity(id = item.browseId, playlistId = item.playlistId, title = item.title, year = item.year, thumbnailUrl = item.thumbnail, songCount = 0, duration = 0, inLibrary = java.time.LocalDateTime.now())) } else -> { }
                }
            }
        }
    }

