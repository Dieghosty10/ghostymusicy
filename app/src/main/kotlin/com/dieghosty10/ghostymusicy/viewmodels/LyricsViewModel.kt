package com.dieghosty10.ghostymusicy.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dieghosty10.ghostymusicy.innertube.YouTube
import com.dieghosty10.ghostymusicy.innertube.models.SongItem
import com.dieghosty10.ghostymusicy.innertube.models.WatchEndpoint
import com.dieghosty10.ghostymusicy.models.MediaMetadata
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LyricsViewModel @Inject constructor() : ViewModel() {

    private val _lyrics = MutableStateFlow<String?>(null)
    val lyrics: StateFlow<String?> = _lyrics.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var fetchJob: Job? = null
    private var currentVideoId: String? = null

    fun fetchLyrics(mediaMetadata: MediaMetadata) {
        val videoId = mediaMetadata.id
        if (videoId == currentVideoId) return
        currentVideoId = videoId
        fetchJob?.cancel()
        
        fetchJob = viewModelScope.launch {
            _isLoading.value = true
            _lyrics.value = null
            
            val query = "${mediaMetadata.title} ${mediaMetadata.artists.firstOrNull()?.name ?: ""} lyrics"
            
            YouTube.next(WatchEndpoint(videoId = videoId)).onSuccess { nextResult ->
                val lyricsEndpoint = nextResult.lyricsEndpoint
                if (lyricsEndpoint != null) {
                    YouTube.lyrics(lyricsEndpoint).onSuccess { lyricsResult ->
                        if (lyricsResult != null) {
                            _lyrics.value = lyricsResult
                        } else {
                            fetchFallback(query)
                        }
                    }.onFailure {
                        fetchFallback(query)
                    }
                } else {
                    fetchFallback(query)
                }
            }.onFailure {
                fetchFallback(query)
            }
            
            _isLoading.value = false
        }
    }

    private suspend fun fetchFallback(query: String) {
        YouTube.search(query, YouTube.SearchFilter.FILTER_SONG).onSuccess { searchResult ->
            val fallbackItem = searchResult.items.firstOrNull { it is SongItem } as? SongItem
            if (fallbackItem != null) {
                YouTube.next(WatchEndpoint(videoId = fallbackItem.id)).onSuccess { nextResult ->
                    val lyricsEndpoint = nextResult.lyricsEndpoint
                    if (lyricsEndpoint != null) {
                        YouTube.lyrics(lyricsEndpoint).onSuccess { lyricsResult ->
                            _lyrics.value = lyricsResult
                        }.onFailure {
                            _lyrics.value = null
                        }
                    } else {
                        _lyrics.value = null
                    }
                }.onFailure {
                    _lyrics.value = null
                }
            } else {
                _lyrics.value = null
            }
        }.onFailure {
            _lyrics.value = null
        }
    }
}
