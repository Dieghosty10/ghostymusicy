package com.dieghosty10.ghostymusicy.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dieghosty10.ghostymusicy.viewmodels.AuthViewModel

@Composable
fun VerificationScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val user by viewModel.currentUser.collectAsState()

    LaunchedEffect(user) {
        if (user?.isEmailVerified == true) {
            onNavigateToHome()
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Rounded.Email,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "Verifica tu Correo",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Te hemos enviado un enlace de confirmación a ${user?.email}. Por favor, revisa tu bandeja de entrada (y la carpeta de spam).",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.checkAuthState()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Ya lo he verificado")
            }

            Spacer(Modifier.height(16.dp))

            TextButton(onClick = { viewModel.sendVerificationEmail() }) {
                Text("Reenviar correo")
            }
            
            Spacer(Modifier.height(8.dp))

            TextButton(onClick = {
                viewModel.logout()
                onNavigateToLogin()
            }) {
                Text("Volver al inicio de sesión")
            }
        }
    }
}
