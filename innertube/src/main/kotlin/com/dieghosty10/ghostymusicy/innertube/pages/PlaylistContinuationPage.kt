/*
 * ghostymusicy Project Original (2026)
 * Dieghosty10 (github.com/Dieghosty10)
 * Licensed Under GPL-3.0 | see git history for contributors
 */



package com.dieghosty10.ghostymusicy.innertube.pages

import com.dieghosty10.ghostymusicy.innertube.models.SongItem

data class PlaylistContinuationPage(
    val songs: List<SongItem>,
    val continuation: String?,
)
