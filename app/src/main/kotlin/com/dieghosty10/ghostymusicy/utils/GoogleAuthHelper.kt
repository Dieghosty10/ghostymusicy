package com.dieghosty10.ghostymusicy.utils

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

object GoogleAuthHelper {

    // IMPORTANT: The user MUST replace this with their actual Web Client ID from Firebase/Google Cloud Console
    private const val WEB_CLIENT_ID = "YOUR_WEB_CLIENT_ID_HERE"

    suspend fun signIn(context: Context): String? {
        if (WEB_CLIENT_ID == "YOUR_WEB_CLIENT_ID_HERE") {
            throw Exception("Falta configurar el Web Client ID en GoogleAuthHelper.kt")
        }

        val credentialManager = CredentialManager.create(context)
        
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(WEB_CLIENT_ID)
            .setNonce(hashedNonce)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val result = credentialManager.getCredential(
                request = request,
                context = context,
            )

            val credential = result.credential
            if (credential is androidx.credentials.CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                return googleIdTokenCredential.idToken
            }
        } catch (e: GetCredentialCancellationException) {
            // User cancelled
            return null
        } catch (e: GetCredentialException) {
            throw Exception(e.localizedMessage ?: "Error al iniciar con Google")
        } catch (e: Exception) {
            throw e
        }
        return null
    }
}
