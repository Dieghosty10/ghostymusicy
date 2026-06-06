package com.dieghosty10.ghostymusicy.ui.screens.settings

import android.content.Context

object DiscordPresenceManager {
    fun isRunning(): Boolean = false
    
    fun updateNow(
        context: Context? = null,
        token: String? = null,
        song: Any? = null,
        positionMs: Long? = null,
        isPaused: Boolean? = null
    ): Boolean = false
    
    fun stop() {}
    
    fun start(
        context: Context? = null,
        token: String? = null,
        songProvider: (() -> Any?)? = null,
        positionProvider: (() -> Long)? = null,
        isPausedProvider: (() -> Boolean)? = null,
        intervalProvider: (() -> Long)? = null
    ) {}
    
    fun restart(): Boolean = false
}

object ListenBrainzManager {
    fun submitPlayingNow(
        context: Context? = null,
        token: String? = null,
        song: Any? = null,
        positionMs: Long? = null
    ) {}
    
    fun submitFinished(
        context: Context? = null,
        token: String? = null,
        song: Any? = null,
        startMs: Long? = null,
        endMs: Long? = null
    ) {}
}
