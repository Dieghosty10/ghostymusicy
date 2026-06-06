/*
 * ghostymusicy Project Original (2026)
 * Dieghosty10 (github.com/Dieghosty10)
 * Licensed Under GPL-3.0 | see git history for contributors
 */



package com.dieghosty10.ghostymusicy.innertube.pages

import com.dieghosty10.ghostymusicy.innertube.models.Album
import com.dieghosty10.ghostymusicy.innertube.models.AlbumItem
import com.dieghosty10.ghostymusicy.innertube.models.Artist
import com.dieghosty10.ghostymusicy.innertube.models.ArtistItem
import com.dieghosty10.ghostymusicy.innertube.models.MusicResponsiveListItemRenderer
import com.dieghosty10.ghostymusicy.innertube.models.MusicTwoRowItemRenderer
import com.dieghosty10.ghostymusicy.innertube.models.PlaylistItem
import com.dieghosty10.ghostymusicy.innertube.models.SongItem
import com.dieghosty10.ghostymusicy.innertube.models.YTItem
import com.dieghosty10.ghostymusicy.innertube.models.oddElements
import com.dieghosty10.ghostymusicy.innertube.utils.parseTime

data class LibraryAlbumsPage(
    val albums: List<AlbumItem>,
    val continuation: String?,
) {
    companion object {
        fun fromMusicTwoRowItemRenderer(renderer: MusicTwoRowItemRenderer): AlbumItem? {
            val browseId = renderer.navigationEndpoint.browseEndpoint?.browseId ?: return null
            val playlistId = renderer.thumbnailOverlay?.musicItemThumbnailOverlayRenderer?.content
                ?.musicPlayButtonRenderer?.playNavigationEndpoint
                ?.watchPlaylistEndpoint?.playlistId
                ?: renderer.menu?.menuRenderer?.items?.firstOrNull()
                    ?.menuNavigationItemRenderer?.navigationEndpoint
                    ?.watchPlaylistEndpoint?.playlistId
                ?: browseId.removePrefix("MPREb_").let { "OLAK5uy_$it" }

            return AlbumItem(
                browseId = browseId,
                playlistId = playlistId,
                title = renderer.title.runs?.firstOrNull()?.text ?: return null,
                artists = null,
                year = renderer.subtitle?.runs?.lastOrNull()?.text?.toIntOrNull(),
                thumbnail = renderer.thumbnailRenderer.musicThumbnailRenderer?.getThumbnailUrl() ?: return null,
                explicit = renderer.subtitleBadges?.find {
                    it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
                } != null
            )
        }
    }
}
