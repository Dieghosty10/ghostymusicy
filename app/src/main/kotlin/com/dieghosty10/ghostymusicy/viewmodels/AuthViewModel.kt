package com.dieghosty10.ghostymusicy.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
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
    private var userListener: ListenerRegistration? = null

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
        
        userListener?.remove()
        userListener = null
        
        auth.currentUser?.let { user ->
            viewModelScope.launch {
                try {
                    user.reload().await()
                    _isEmailVerified.value = user.isEmailVerified
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            userListener = firestore.collection("users").document(user.uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    
                    if (snapshot != null && snapshot.exists()) {
                        _userRole.value = snapshot.getString("role") ?: "user"
                        val suspended = snapshot.getBoolean("isSuspended") ?: false
                        _isSuspended.value = suspended
                        if (suspended) {
                            logout()
                        }
                    } else {
                        // User profile was deleted
                        logout()
                    }
                }
        }
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = auth.signInWithEmailAndPassword(email, pass).await()
                result.user?.let { user ->
                    val doc = firestore.collection("users").document(user.uid).get().await()
                    val suspended = doc.getBoolean("isSuspended") ?: false
                    
                    if (suspended) {
                        _error.value = "Tu cuenta está suspendida."
                        auth.signOut()
                    } else {
                        checkAuthState()
                        onSuccess()
                    }
                } ?: run {
                    _error.value = "No se pudo obtener la información del usuario."
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

    fun loginWithGoogle(idToken: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                result.user?.let { user ->
                    // Check if user exists in Firestore
                    val doc = firestore.collection("users").document(user.uid).get().await()
                    if (!doc.exists()) {
                        // Create profile for new Google user
                        val userData = hashMapOf(
                            "email" to (user.email ?: ""),
                            "role" to "user",
                            "isSuspended" to false,
                            "createdAt" to System.currentTimeMillis()
                        )
                        firestore.collection("users").document(user.uid).set(userData).await()
                    } else {
                        val suspended = doc.getBoolean("isSuspended") ?: false
                        if (suspended) {
                            _error.value = "Tu cuenta está suspendida."
                            auth.signOut()
                            return@launch
                        }
                    }
                    checkAuthState()
                    onSuccess()
                } ?: run {
                    _error.value = "Error al autenticar con Google."
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error en Google Sign-In"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendVerificationEmail() {
        auth.currentUser?.sendEmailVerification()
    }

    fun logout() {
        userListener?.remove()
        userListener = null
        auth.signOut()
        _currentUser.value = null
    }

    fun clearError() {
        _error.value = null
    }
}
