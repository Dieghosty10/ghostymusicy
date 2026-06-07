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

data class UserModel(
    val id: String = "",
    val email: String = "",
    val role: String = "user",
    val isSuspended: Boolean = false,
    val createdAt: Long = 0L
)

@HiltViewModel
class AdminViewModel @Inject constructor() : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _users = MutableStateFlow<List<UserModel>>(emptyList())
    val users: StateFlow<List<UserModel>> = _users.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = firestore.collection("users").get().await()
                val list = result.documents.map { doc ->
                    UserModel(
                        id = doc.id,
                        email = doc.getString("email") ?: "",
                        role = doc.getString("role") ?: "user",
                        isSuspended = doc.getBoolean("isSuspended") ?: false,
                        createdAt = doc.getLong("createdAt") ?: 0L
                    )
                }.sortedByDescending { it.createdAt }
                _users.value = list
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error al cargar usuarios"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleSuspendStatus(userId: String, suspend: Boolean) {
        viewModelScope.launch {
            try {
                firestore.collection("users").document(userId).update("isSuspended", suspend).await()
                _successMessage.value = if (suspend) "Usuario suspendido" else "Usuario restaurado"
                loadUsers()
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error al suspender"
            }
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            try {
                firestore.collection("users").document(userId).delete().await()
                _successMessage.value = "Usuario eliminado (perfil)"
                loadUsers()
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error al eliminar"
            }
        }
    }

    fun sendGlobalNotification(title: String, message: String) {
        viewModelScope.launch {
            try {
                val notif = mapOf(
                    "title" to title,
                    "message" to message,
                    "createdAt" to System.currentTimeMillis()
                )
                firestore.collection("global_notifications").add(notif).await()
                _successMessage.value = "Notificacin enviada"
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error al enviar notificacin"
            }
        }
    }

    fun clearMessages() {
        _error.value = null
        _successMessage.value = null
    }
}
