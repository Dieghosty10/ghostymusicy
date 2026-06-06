package com.dieghosty10.ghostymusicy.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dieghosty10.ghostymusicy.db.MusicDatabase
import com.dieghosty10.ghostymusicy.db.entities.EventWithSong
import com.dieghosty10.ghostymusicy.innertube.YouTube
import com.dieghosty10.ghostymusicy.innertube.pages.HomePage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
    private val database: MusicDatabase
) : ViewModel() {

    private val _homePage = MutableStateFlow<HomePage?>(null)
    val homePage: StateFlow<HomePage?> = _homePage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _heroArtist = MutableStateFlow<com.dieghosty10.ghostymusicy.innertube.pages.ArtistPage?>(null)
    val heroArtist: StateFlow<com.dieghosty10.ghostymusicy.innertube.pages.ArtistPage?> = _heroArtist.asStateFlow()

    private val _newReleases = MutableStateFlow<HomePage.Section?>(null)
    val newReleases: StateFlow<HomePage.Section?> = _newReleases.asStateFlow()

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
            
            launch {
                try {
                    val homeData = YouTube.home().getOrNull()
                    val releases = homeData?.sections?.find {
                        val t = it.title.lowercase()
                        t.contains("lanzamiento") || t.contains("nuevo") || t.contains("release")
                    }
                    _newReleases.value = releases
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val customSections = mutableListOf<HomePage.Section>()
            // Leer favoritos
                val favoritesStr = com.dieghosty10.ghostymusicy.utils.PreferenceStore.get(
                    com.dieghosty10.ghostymusicy.constants.SelectedFavoriteArtistsKey
                )
                if (!favoritesStr.isNullOrEmpty()) {
                    val favList = favoritesStr.split(",")
                    val heroArtistId = favList.firstOrNull()
                    if (heroArtistId != null) {
                        try {
                            _heroArtist.value = YouTube.artist(heroArtistId).getOrNull()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    val randomFavs = favList.shuffled().take(3) // Tomar 3 artistas al azar
                    val sections = coroutineScope {
                        randomFavs.map { favId ->
                            async {
                                try {
                                    val artistPage = YouTube.artist(favId).getOrNull() ?: return@async null
                                    val songsSection = artistPage.sections?.find { it.title.contains("Canciones", ignoreCase = true) || it.title.contains("Songs", ignoreCase = true) }
                                    val albumsSection = artistPage.sections?.find { it.title.contains("Álbum", ignoreCase = true) || it.title.contains("Album", ignoreCase = true) }

                                    val items = mutableListOf<com.dieghosty10.ghostymusicy.innertube.models.YTItem>()
                                    songsSection?.items?.take(5)?.let { items.addAll(it) }
                                    albumsSection?.items?.take(5)?.let { items.addAll(it) }

                                    if (items.isNotEmpty()) {
                                        HomePage.Section(
                                            title = "Porque te gusta ${artistPage.artist.title}",
                                            label = null,
                                            thumbnail = null,
                                            endpoint = null,
                                            items = items.shuffled()
                                        )
                                    } else null
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    null
                                }
                            }
                        }.awaitAll().filterNotNull()
                    }
                    customSections.addAll(sections)
                }

            if (customSections.isNotEmpty()) {
                _homePage.value = HomePage(sections = customSections, chips = null, continuation = null)
            } else {
                // Sin favoritos configurados → UI mostrará estado vacío
                _homePage.value = null
            }
            _isLoading.value = false
        }
    }
}
