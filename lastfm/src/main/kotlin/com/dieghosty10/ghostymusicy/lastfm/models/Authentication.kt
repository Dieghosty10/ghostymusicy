/*
 * ghostymusicy Project Original (2026)
 * Dieghosty10 (github.com/Dieghosty10)
 * Licensed Under GPL-3.0 | see git history for contributors
 */



package com.dieghosty10.ghostymusicy.lastfm.models

import kotlinx.serialization.Serializable

@Serializable
data class Authentication(
    val session: Session
) {
    @Serializable
    data class Session(
        val name: String,       // Username
        val key: String,        // Session Key
        val subscriber: Int,    // Last.fm Pro?
    )
}

@Serializable
data class TokenResponse(
    val token: String
)

@Serializable
data class LastFmError(
    val error: Int,
    val message: String
)
