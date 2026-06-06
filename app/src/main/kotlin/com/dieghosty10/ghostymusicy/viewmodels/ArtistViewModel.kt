package com.dieghosty10.ghostymusicy.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dieghosty10.ghostymusicy.innertube.YouTube
import com.dieghosty10.ghostymusicy.innertube.pages.ArtistPage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArtistViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val artistId: String = checkNotNull(savedStateHandle["artistId"])

    private val _artistPage = MutableStateFlow<ArtistPage?>(null)
    val artistPage: StateFlow<ArtistPage?> = _artistPage.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        fetchArtist()
    }

    fun fetchArtist() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            YouTube.artist(artistId).onSuccess { page ->
                _artistPage.value = page
            }.onFailure { e ->
                _error.value = e.message ?: "Error al cargar el artista"
            }
            _isLoading.value = false
        }
    }
}
