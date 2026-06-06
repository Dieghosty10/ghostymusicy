package com.dieghosty10.ghostymusicy.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dieghosty10.ghostymusicy.innertube.YouTube
import com.dieghosty10.ghostymusicy.innertube.models.ArtistItem
import com.dieghosty10.ghostymusicy.innertube.models.YTItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor() : ViewModel() {

    private val _suggestedArtists = MutableStateFlow<List<ArtistItem>>(emptyList())
    val suggestedArtists: StateFlow<List<ArtistItem>> = _suggestedArtists.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedArtists = MutableStateFlow<Set<String>>(emptySet())
    val selectedArtists: StateFlow<Set<String>> = _selectedArtists.asStateFlow()

    init {
        loadSuggestedArtists()
    }

    private fun loadSuggestedArtists() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Fetch very famous artists directly to ensure high quality initial list
                val allQueries = listOf("Bad Bunny", "Taylor Swift", "The Weeknd", "Feid", "Karol G", "Drake", "Dua Lipa", "Shakira", "Bruno Mars", "Billie Eilish", "J Balvin", "Rauw Alejandro", "Ariana Grande", "Coldplay", "Eminem", "Imagine Dragons", "Ed Sheeran", "Rosalía", "Quevedo")
                val famousQueries = allQueries.shuffled().take(8)
                
                // Fetch concurrently to be fast
                val artists = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val deferredList = mutableListOf<kotlinx.coroutines.Deferred<ArtistItem?>>()
                    for (query in famousQueries) {
                        deferredList.add(async {
                            val result = YouTube.search(query, YouTube.SearchFilter.FILTER_ARTIST).getOrNull()
                            result?.items?.filterIsInstance<ArtistItem>()?.firstOrNull()
                        })
                    }
                    deferredList.mapNotNull { it.await() }
                }
                
                _suggestedArtists.value = artists.distinctBy { it.id }.shuffled()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchArtists(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = YouTube.search(query, YouTube.SearchFilter.FILTER_ARTIST).getOrNull()
                val searched = result?.items?.filterIsInstance<ArtistItem>() ?: emptyList()
                if (searched.isNotEmpty()) {
                    val currentList = _suggestedArtists.value.toMutableList()
                    // Add new searched artists at the top
                    val combined = (searched + currentList).distinctBy { it.id }
                    _suggestedArtists.value = combined
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleArtistSelection(artistId: String) {
        val current = _selectedArtists.value.toMutableSet()
        val isSelecting = !current.contains(artistId)
        if (isSelecting) {
            current.add(artistId)
            fetchRelatedArtists(artistId)
        } else {
            current.remove(artistId)
        }
        _selectedArtists.value = current
    }

    private fun fetchRelatedArtists(artistId: String) {
        viewModelScope.launch {
            try {
                val artistPage = YouTube.artist(artistId).getOrNull() ?: return@launch
                val relatedSection = artistPage.sections?.find { 
                    it.title.contains("fans", ignoreCase = true) || 
                    it.title.contains("relacionados", ignoreCase = true) ||
                    it.title.contains("similares", ignoreCase = true)
                }
                val relatedArtists = relatedSection?.items?.filterIsInstance<ArtistItem>() ?: emptyList()
                
                if (relatedArtists.isNotEmpty()) {
                    val currentList = _suggestedArtists.value.toMutableList()
                    // Insert related artists right after the currently selected artist, or at the top
                    val combined = (relatedArtists.take(5) + currentList).distinctBy { it.id }
                    _suggestedArtists.value = combined
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
