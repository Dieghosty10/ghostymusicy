package com.dieghosty10.ghostymusicy.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dieghosty10.ghostymusicy.db.MusicDatabase
import com.dieghosty10.ghostymusicy.db.entities.EventWithSong
import com.dieghosty10.ghostymusicy.innertube.YouTube
import com.dieghosty10.ghostymusicy.innertube.pages.HomePage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val database: MusicDatabase
) : ViewModel() {

    private val _homePage = MutableStateFlow<HomePage?>(null)
    val homePage: StateFlow<HomePage?> = _homePage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Historial de escucha para personalizar sugerencias en el inicio
    val recentEvents: StateFlow<List<EventWithSong>> =
        database.events()
            .map { it.take(20) }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        fetchHome()
    }

    fun fetchHome() {
        viewModelScope.launch {
            _isLoading.value = true
            YouTube.home().onSuccess { _homePage.value = it }
            _isLoading.value = false
        }
    }
}
