package com.dieghosty10.ghostymusicy.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dieghosty10.ghostymusicy.db.MusicDatabase
import com.dieghosty10.ghostymusicy.db.entities.Album
import com.dieghosty10.ghostymusicy.db.entities.Artist
import com.dieghosty10.ghostymusicy.db.entities.Playlist
import com.dieghosty10.ghostymusicy.db.entities.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val database: MusicDatabase
) : ViewModel() {

    val likedSongs: StateFlow<List<Song>> = database.likedSongsByCreateDateAsc()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val playlists: StateFlow<List<Playlist>> = database.playlistsBySongCountAsc()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val customPlaylists: StateFlow<List<Playlist>> = database.getLocalPlaylists()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val likedAlbums: StateFlow<List<Album>> = database.albumsLikedByLengthAsc()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val likedArtists: StateFlow<List<Artist>> = database.artistsBookmarkedBySongCountAsc()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}
