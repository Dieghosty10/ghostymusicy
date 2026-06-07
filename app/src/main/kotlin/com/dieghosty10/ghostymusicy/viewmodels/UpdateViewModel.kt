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

data class AppUpdateInfo(
    val latestVersionCode: Int = 0,
    val latestVersionName: String = "",
    val downloadUrl: String = "",
    val forceUpdate: Boolean = false
)

@HiltViewModel
class UpdateViewModel @Inject constructor() : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

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
                val doc = firestore.collection("app_config").document("update_info").get().await()
                if (doc.exists()) {
                    val info = AppUpdateInfo(
                        latestVersionCode = doc.getLong("latestVersionCode")?.toInt() ?: 0,
                        latestVersionName = doc.getString("latestVersionName") ?: "",
                        downloadUrl = doc.getString("downloadUrl") ?: "",
                        forceUpdate = doc.getBoolean("forceUpdate") ?: false
                    )
                    _updateInfo.value = info
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            } finally {
                _isChecking.value = false
            }
        }
    }
}
