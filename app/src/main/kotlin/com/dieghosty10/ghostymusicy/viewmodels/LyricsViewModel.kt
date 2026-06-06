package com.dieghosty10.ghostymusicy.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dieghosty10.ghostymusicy.innertube.YouTube
import com.dieghosty10.ghostymusicy.innertube.models.WatchEndpoint
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

    fun fetchLyrics(videoId: String) {
        if (videoId == currentVideoId) return
        currentVideoId = videoId
        fetchJob?.cancel()
        
        fetchJob = viewModelScope.launch {
            _isLoading.value = true
            _lyrics.value = null
            
            YouTube.next(WatchEndpoint(videoId = videoId)).onSuccess { nextResult ->
                val lyricsEndpoint = nextResult.lyricsEndpoint
                if (lyricsEndpoint != null) {
                    YouTube.lyrics(lyricsEndpoint).onSuccess { lyricsResult ->
                        _lyrics.value = lyricsResult ?: "Letra no disponible"
                    }.onFailure {
                        _lyrics.value = "Letra no disponible"
                    }
                } else {
                    _lyrics.value = "Letra no disponible"
                }
            }.onFailure {
                _lyrics.value = "Letra no disponible"
            }
            
            _isLoading.value = false
        }
    }
}
