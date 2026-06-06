/*
 * ghostymusicy Project Original (2026)
 * Dieghosty10 (github.com/Dieghosty10)
 * Licensed Under GPL-3.0 | see git history for contributors
 */



package com.dieghosty10.ghostymusicy.extensions

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.dieghosty10.ghostymusicy.constants.InnerTubeCookieKey
import com.dieghosty10.ghostymusicy.constants.YtmSyncKey
import com.dieghosty10.ghostymusicy.utils.dataStore
import com.dieghosty10.ghostymusicy.utils.get
import com.dieghosty10.ghostymusicy.innertube.utils.parseCookieString

fun Context.isSyncEnabled(): Boolean {
    return dataStore.get(YtmSyncKey, true) && isUserLoggedIn()
}

fun Context.isUserLoggedIn(): Boolean {
    val cookie = dataStore[InnerTubeCookieKey] ?: ""
    return "SAPISID" in parseCookieString(cookie) && isInternetConnected()
}

fun Context.isInternetConnected(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
}
