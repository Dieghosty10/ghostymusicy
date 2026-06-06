/*
 * ghostymusicy Project Original (2026)
 * Dieghosty10 (github.com/Dieghosty10)
 * Licensed Under GPL-3.0 | see git history for contributors
 */



package com.dieghosty10.ghostymusicy.innertube.pages

import com.dieghosty10.ghostymusicy.innertube.models.YTItem
import com.dieghosty10.ghostymusicy.innertube.models.filterExplicit
import com.dieghosty10.ghostymusicy.innertube.models.filterVideo

data class BrowseResult(
    val title: String?,
    val items: List<Item>,
) {
    data class Item(
        val title: String?,
        val items: List<YTItem>,
    )

    fun filterExplicit(enabled: Boolean = true) =
        if (enabled) {
            copy(
                items =
                    items.mapNotNull {
                        it.copy(
                            items =
                                it.items
                                    .filterExplicit()
                                    .ifEmpty { return@mapNotNull null },
                        )
                    },
            )
        } else {
            this
        }

    fun filterVideo(enabled: Boolean = true) =
        if (enabled) {
            copy(
                items =
                    items.mapNotNull {
                        it.copy(
                            items =
                                it.items
                                    .filterVideo()
                                    .ifEmpty { return@mapNotNull null },
                        )
                    },
            )
        } else {
            this
        }
}
