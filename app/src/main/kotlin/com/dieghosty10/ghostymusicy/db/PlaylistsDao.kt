package com.dieghosty10.ghostymusicy.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.dieghosty10.ghostymusicy.db.entities.PlaylistEntity
import com.dieghosty10.ghostymusicy.db.entities.PlaylistSongMap
import com.dieghosty10.ghostymusicy.db.entities.Playlist
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSongMap(map: PlaylistSongMap)

    @Query("SELECT *, (SELECT COUNT(*) FROM playlist_song_map WHERE playlistId = playlist.id) AS songCount FROM playlist WHERE isLocal = 1 ORDER BY createdAt DESC")
    fun getLocalPlaylists(): Flow<List<Playlist>>

    @Transaction
    suspend fun addSongToCustomPlaylist(playlistId: String, songId: String, position: Int) {
        insertPlaylistSongMap(PlaylistSongMap(playlistId = playlistId, songId = songId, position = position))
    }
}
