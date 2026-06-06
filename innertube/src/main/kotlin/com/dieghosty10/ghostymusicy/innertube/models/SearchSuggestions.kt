/*
 * ghostymusicy Project Original (2026)
 * Dieghosty10 (github.com/Dieghosty10)
 * Licensed Under GPL-3.0 | see git history for contributors
 */



package com.dieghosty10.ghostymusicy.innertube.models

data class SearchSuggestions(
    val queries: List<String>,
    val recommendedItems: List<YTItem>,
)
