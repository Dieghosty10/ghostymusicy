package com.dieghosty10.ghostymusicy.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dieghosty10.ghostymusicy.viewmodels.AdminViewModel
import com.dieghosty10.ghostymusicy.viewmodels.UserData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val users by viewModel.users.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Administrador") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(users) { user ->
                    UserCard(
                        user = user,
                        onToggleSuspend = { suspend ->
                            viewModel.toggleSuspend(user.uid, suspend)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun UserCard(user: UserData, onToggleSuspend: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (user.isSuspended) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (user.isSuspended) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Rol: ${user.role}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (user.isSuspended) MaterialTheme.colorScheme.onErrorContainer.copy(alpha=0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (user.role != "admin") {
                Switch(
                    checked = user.isSuspended,
                    onCheckedChange = onToggleSuspend,
                    thumbContent = {
                        if (user.isSuspended) Icon(Icons.Rounded.Block, null) else Icon(Icons.Rounded.CheckCircle, null)
                    }
                )
            }
        }
    }
}
