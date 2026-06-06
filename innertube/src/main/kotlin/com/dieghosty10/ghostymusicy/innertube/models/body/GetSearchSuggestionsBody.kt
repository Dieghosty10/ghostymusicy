/*
 * ghostymusicy Project Original (2026)
 * Dieghosty10 (github.com/Dieghosty10)
 * Licensed Under GPL-3.0 | see git history for contributors
 */



package com.dieghosty10.ghostymusicy.innertube.models.body

import com.dieghosty10.ghostymusicy.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class GetSearchSuggestionsBody(
    val context: Context,
    val input: String,
)
