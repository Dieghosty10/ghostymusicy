/*
 * ghostymusicy Project Original (2026)
 * Dieghosty10 (github.com/Dieghosty10)
 * Licensed Under GPL-3.0 | see git history for contributors
 */



package com.dieghosty10.ghostymusicy.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionButton(
    val subscribeButtonRenderer: SubscribeButtonRenderer,
) {
    @Serializable
    data class SubscribeButtonRenderer(
        val subscribed: Boolean,
        val channelId: String,
    )
}
