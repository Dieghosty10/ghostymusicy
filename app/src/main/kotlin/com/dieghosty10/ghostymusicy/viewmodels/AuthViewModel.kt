package com.dieghosty10.ghostymusicy.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _userRole = MutableStateFlow<String>("user")
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    private val _isSuspended = MutableStateFlow<Boolean>(false)
    val isSuspended: StateFlow<Boolean> = _isSuspended.asStateFlow()

    private val _isEmailVerified = MutableStateFlow<Boolean>(auth.currentUser?.isEmailVerified == true)
    val isEmailVerified: StateFlow<Boolean> = _isEmailVerified.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        checkAuthState()
    }

    fun checkAuthState() {
        _currentUser.value = auth.currentUser
        _isEmailVerified.value = auth.currentUser?.isEmailVerified == true
        auth.currentUser?.let { user ->
            viewModelScope.launch {
                try {
                    user.reload().await()
                    _isEmailVerified.value = user.isEmailVerified
                    val doc = firestore.collection("users").document(user.uid).get().await()
                    _userRole.value = doc.getString("role") ?: "user"
                    _isSuspended.value = doc.getBoolean("isSuspended") ?: false
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                auth.signInWithEmailAndPassword(email, pass).await()
                checkAuthState()
                if (_isSuspended.value) {
                    _error.value = "Tu cuenta está suspendida."
                    auth.signOut()
                } else {
                    onSuccess()
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error al iniciar sesión"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = auth.createUserWithEmailAndPassword(email, pass).await()
                result.user?.let { user ->
                    user.sendEmailVerification().await()
                    val userData = hashMapOf(
                        "email" to email,
                        "role" to "user",
                        "isSuspended" to false,
                        "createdAt" to System.currentTimeMillis()
                    )
                    firestore.collection("users").document(user.uid).set(userData).await()
                    checkAuthState()
                    onSuccess()
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error al registrar"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendVerificationEmail() {
        auth.currentUser?.sendEmailVerification()
    }

    fun logout() {
        auth.signOut()
        _currentUser.value = null
    }

    fun clearError() {
        _error.value = null
    }
}
