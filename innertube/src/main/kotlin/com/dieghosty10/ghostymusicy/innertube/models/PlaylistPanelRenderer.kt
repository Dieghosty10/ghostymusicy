/*
 * ghostymusicy Project Original (2026)
 * Dieghosty10 (github.com/Dieghosty10)
 * Licensed Under GPL-3.0 | see git history for contributors
 */



package com.dieghosty10.ghostymusicy.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistPanelRenderer(
    val title: String?,
    val titleText: Runs?,
    val shortBylineText: Runs?,
    val contents: List<Content>,
    val isInfinite: Boolean?,
    val numItemsToShow: Int?,
    val playlistId: String?,
    val continuations: List<Continuation>?,
) {
    @Serializable
    data class Content(
        val playlistPanelVideoRenderer: PlaylistPanelVideoRenderer?,
        val automixPreviewVideoRenderer: AutomixPreviewVideoRenderer?,
    )
}
