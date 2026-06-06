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

data class UserData(
    val uid: String,
    val email: String,
    val role: String,
    val isSuspended: Boolean
)

@HiltViewModel
class AdminViewModel @Inject constructor() : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _users = MutableStateFlow<List<UserData>>(emptyList())
    val users: StateFlow<List<UserData>> = _users.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = firestore.collection("users").get().await()
                val list = snapshot.documents.mapNotNull { doc ->
                    val email = doc.getString("email") ?: return@mapNotNull null
                    UserData(
                        uid = doc.id,
                        email = email,
                        role = doc.getString("role") ?: "user",
                        isSuspended = doc.getBoolean("isSuspended") ?: false
                    )
                }
                _users.value = list
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleSuspend(uid: String, suspend: Boolean) {
        viewModelScope.launch {
            try {
                firestore.collection("users").document(uid).update("isSuspended", suspend).await()
                loadUsers()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
