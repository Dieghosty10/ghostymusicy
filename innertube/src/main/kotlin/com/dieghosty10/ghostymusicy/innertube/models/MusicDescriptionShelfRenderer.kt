/*
 * ghostymusicy Project Original (2026)
 * Dieghosty10 (github.com/Dieghosty10)
 * Licensed Under GPL-3.0 | see git history for contributors
 */



package com.dieghosty10.ghostymusicy.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class MusicDescriptionShelfRenderer(
    val header: Runs?,
    val subheader: Runs?,
    val description: Runs,
    val footer: Runs?,
)
