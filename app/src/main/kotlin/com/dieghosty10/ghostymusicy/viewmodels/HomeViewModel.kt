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
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import com.google.gson.GsonBuilder
import com.dieghosty10.ghostymusicy.utils.YTItemAdapter
import com.dieghosty10.ghostymusicy.innertube.models.YTItem
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

@HiltViewModel
class HomeViewModel @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
    private val database: MusicDatabase
) : ViewModel() {

    private val _homePage = MutableStateFlow<HomePage?>(null)
    val homePage: StateFlow<HomePage?> = _homePage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val prefs = context.getSharedPreferences("home_cache", android.content.Context.MODE_PRIVATE)
    private val gson = GsonBuilder()
        .registerTypeAdapter(YTItem::class.java, YTItemAdapter())
        .create()

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
        val cached = prefs.getString("home_page_cache", null)
        if (cached != null) {
            try {
                _homePage.value = gson.fromJson(cached, HomePage::class.java)
            } catch(e: Exception) { e.printStackTrace() }
        }
        fetchHome()
        rotateHeroArtist()
    }

    fun rotateHeroArtist() {
        viewModelScope.launch(Dispatchers.IO) {
            val favoritesStr = com.dieghosty10.ghostymusicy.utils.PreferenceStore.get(
                com.dieghosty10.ghostymusicy.constants.SelectedFavoriteArtistsKey
            )
            if (!favoritesStr.isNullOrEmpty()) {
                val favList = favoritesStr.split(",")
                // Get a random artist different from the current one if possible
                val currentId = _heroArtist.value?.artist?.id
                var newId = favList.randomOrNull()
                if (favList.size > 1 && newId == currentId) {
                    newId = favList.filter { it != currentId }.randomOrNull()
                }
                if (newId != null) {
                    try {
                        _heroArtist.value = YouTube.artist(newId).getOrNull()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun fetchHome() {
        viewModelScope.launch {
            if (!isNetworkAvailable()) {
                _isOffline.value = true
                if (_homePage.value == null) {
                    _isLoading.value = false
                }
                return@launch
            }
            _isOffline.value = false
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
                val page = HomePage(sections = customSections, chips = null, continuation = null)
                _homePage.value = page
                prefs.edit().putString("home_page_cache", gson.toJson(page)).apply()
            } else {
                // Sin favoritos configurados → UI mostrará estado vacío
                if (_homePage.value == null) {
                    _homePage.value = null
                }
            }
            _isLoading.value = false
        }
    }
}
