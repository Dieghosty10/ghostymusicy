package com.dieghosty10.ghostymusicy.viewmodels

import androidx.lifecycle.ViewModel
import com.dieghosty10.ghostymusicy.db.MusicDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class DummySong(val title: String, val artist: String, val thumbnailUrl: String)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val database: MusicDatabase
) : ViewModel() {

    private val _dominantColor = MutableStateFlow<Int?>(null)
    val dominantColor: StateFlow<Int?> = _dominantColor.asStateFlow()

    private val _currentSong = MutableStateFlow<DummySong?>(
        DummySong(
            title = "Never Gonna Give You Up",
            artist = "Rick Astley",
            thumbnailUrl = "https://i.ytimg.com/vi/dQw4w9WgXcQ/maxresdefault.jpg"
        )
    )
    val currentSong: StateFlow<DummySong?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    fun updateDominantColor(color: Int?) {
        _dominantColor.value = color
    }

    fun play() { _isPlaying.value = true }
    fun pause() { _isPlaying.value = false }
    fun skipNext() {}
    fun skipPrevious() {}
}
