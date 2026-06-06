package com.dieghosty10.ghostymusicy.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dieghosty10.ghostymusicy.innertube.YouTube
import com.dieghosty10.ghostymusicy.innertube.pages.AlbumPage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val albumId: String = checkNotNull(savedStateHandle["albumId"])

    private val _albumPage = MutableStateFlow<AlbumPage?>(null)
    val albumPage: StateFlow<AlbumPage?> = _albumPage.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        fetchAlbum()
    }

    fun fetchAlbum() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            YouTube.album(albumId).onSuccess { page ->
                _albumPage.value = page
            }.onFailure { e ->
                _error.value = e.message ?: "Error al cargar el álbum"
            }
            _isLoading.value = false
        }
    }
}
