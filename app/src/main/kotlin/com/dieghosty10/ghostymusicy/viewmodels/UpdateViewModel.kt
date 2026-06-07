package com.dieghosty10.ghostymusicy.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import com.dieghosty10.ghostymusicy.BuildConfig

data class AppUpdateInfo(
    val latestVersionName: String = "",
    val downloadUrl: String = "",
    val forceUpdate: Boolean = false
)

@HiltViewModel
class UpdateViewModel @Inject constructor() : ViewModel() {

    private val _updateInfo = MutableStateFlow<AppUpdateInfo?>(null)
    val updateInfo: StateFlow<AppUpdateInfo?> = _updateInfo.asStateFlow()

    private val _isChecking = MutableStateFlow(false)
    val isChecking: StateFlow<Boolean> = _isChecking.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun checkForUpdates() {
        viewModelScope.launch {
            _isChecking.value = true
            try {
                withContext(Dispatchers.IO) {
                    val url = URL("https://api.github.com/repos/Dieghosty10/ghostymusicy/releases/latest")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                    
                    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                        val response = connection.inputStream.bufferedReader().use { it.readText() }
                        val json = JSONObject(response)
                        val tagName = json.getString("tag_name") // e.g., "v1.0.2"
                        val assets = json.getJSONArray("assets")
                        var downloadUrl = ""
                        if (assets.length() > 0) {
                            downloadUrl = assets.getJSONObject(0).getString("browser_download_url")
                        }
                        
                        val isNewer = compareVersions(tagName, BuildConfig.VERSION_NAME) > 0
                        
                        if (isNewer) {
                            val info = AppUpdateInfo(
                                latestVersionName = tagName,
                                downloadUrl = downloadUrl,
                                forceUpdate = tagName.contains("force", ignoreCase = true) // If tag contains "force", it's forced
                            )
                            _updateInfo.value = info
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            } finally {
                _isChecking.value = false
            }
        }
    }
    
    private fun compareVersions(v1: String, v2: String): Int {
        val normalize = { v: String -> v.replace("v", "").replace("V", "").split(".").map { it.toIntOrNull() ?: 0 } }
        val parts1 = normalize(v1)
        val parts2 = normalize(v2)
        val length = maxOf(parts1.size, parts2.size)
        for (i in 0 until length) {
            val p1 = parts1.getOrElse(i) { 0 }
            val p2 = parts2.getOrElse(i) { 0 }
            if (p1 > p2) return 1
            if (p1 < p2) return -1
        }
        return 0
    }
}
