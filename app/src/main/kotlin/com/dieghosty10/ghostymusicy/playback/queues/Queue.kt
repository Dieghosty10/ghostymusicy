/*
 * ghostymusicy Project Original (2026)
 * Dieghosty10 (github.com/Dieghosty10)
 * Licensed Under GPL-3.0 | see git history for contributors
 */



package com.dieghosty10.ghostymusicy.playback.queues

import androidx.media3.common.MediaItem
import com.dieghosty10.ghostymusicy.extensions.ExtraIsMusicVideo
import com.dieghosty10.ghostymusicy.extensions.metadata
import com.dieghosty10.ghostymusicy.models.MediaMetadata

interface Queue {
    val preloadItem: MediaMetadata?

    suspend fun getInitialStatus(): Status

    fun hasNextPage(): Boolean

    suspend fun nextPage(): List<MediaItem>

    data class Status(
        val title: String?,
        val items: List<MediaItem>,
        val mediaItemIndex: Int,
        val position: Long = 0L,
    ) {
        fun filterExplicit(enabled: Boolean = true) =
            if (enabled) {
                copy(
                    items = items.filterExplicit(),
                )
            } else {
                this
            }
        fun filterVideo(enabled: Boolean = true) =
            if (enabled) {
                copy(
                    items = items.filterVideo(),
                )
            } else {
                this
            }
    }
}

fun List<MediaItem>.filterExplicit(enabled: Boolean = true) =
    if (enabled) {
        filterNot {
            it.metadata?.explicit == true
        }
    } else {
        this
    }

fun List<MediaItem>.filterVideo(enabled: Boolean = true) =
    if (enabled) {
        filterNot {
            it.mediaMetadata.extras?.getBoolean(ExtraIsMusicVideo, false) == true
        }
    } else {
        this
    }
