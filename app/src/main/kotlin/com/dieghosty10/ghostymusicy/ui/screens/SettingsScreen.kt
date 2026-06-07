package com.dieghosty10.ghostymusicy.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dieghosty10.ghostymusicy.R
import com.dieghosty10.ghostymusicy.constants.CustomThemeColorKey
import com.dieghosty10.ghostymusicy.constants.DarkModeKey
import com.dieghosty10.ghostymusicy.ui.theme.DarkMode
import com.dieghosty10.ghostymusicy.ui.theme.GhostyAccent
import com.dieghosty10.ghostymusicy.ui.theme.accentFromName
import com.dieghosty10.ghostymusicy.viewmodels.AuthViewModel
import com.dieghosty10.ghostymusicy.ui.navigation.Routes
import com.dieghosty10.ghostymusicy.utils.rememberEnumPreference
import com.dieghosty10.ghostymusicy.utils.rememberPreference

@Composable
fun SettingsScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val darkMode by rememberEnumPreference(DarkModeKey, DarkMode.ON)
    var darkModeState by rememberEnumPreference(DarkModeKey, DarkMode.ON)

    val accentName by rememberPreference(CustomThemeColorKey, GhostyAccent.BLUE.name)
    var accentNameState by rememberPreference(CustomThemeColorKey, GhostyAccent.BLUE.name)

    val userRole by authViewModel.userRole.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // ── Header ────────────────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Ghostymusicy",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "v1.0.0  •  by @ghosty",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // ── SECCIÓN: APARIENCIA ──────────────────────────────────────────
        item { SettingsSectionHeader("Apariencia") }

        // Tema
        item {
            SettingsCard {
                Text(
                    "Tema",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeChip(
                        label = "Oscuro",
                        icon = Icons.Rounded.DarkMode,
                        selected = darkModeState == DarkMode.ON,
                        onClick = { darkModeState = DarkMode.ON },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeChip(
                        label = "Claro",
                        icon = Icons.Rounded.LightMode,
                        selected = darkModeState == DarkMode.OFF,
                        onClick = { darkModeState = DarkMode.OFF },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeChip(
                        label = "Sistema",
                        icon = Icons.Rounded.PhoneAndroid,
                        selected = darkModeState == DarkMode.FOLLOW_SYSTEM,
                        onClick = { darkModeState = DarkMode.FOLLOW_SYSTEM },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Color de acento
        item {
            SettingsCard {
                Text(
                    "Color de acento",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                val rows = GhostyAccent.entries.chunked(4)
                rows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { accent ->
                            val isSelected = accentNameState == accent.name
                            val animColor by animateColorAsState(accent.color, tween(300))
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { accentNameState = accent.name }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(animColor)
                                        .then(
                                            if (isSelected) Modifier.border(3.dp, Color.White, CircleShape)
                                            else Modifier.border(2.dp, Color.Transparent, CircleShape)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            Icons.Rounded.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    accent.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── SECCIÓN: REPRODUCCIÓN ─────────────────────────────────────────
        item { SettingsSectionHeader("Reproducción") }
        item {
            var smartRadioState by rememberPreference(com.dieghosty10.ghostymusicy.constants.SmartRadioKey, true)
            SettingsCard {
                SettingsSwitchRow(
                    icon = Icons.Rounded.Radio,
                    title = "Smart Radio",
                    subtitle = "Continúa reproduciendo canciones similares al terminar la cola",
                    checked = smartRadioState,
                    onCheckedChange = { smartRadioState = it }
                )
            }
        }

        // 🎵 SECCIÓN: CUENTA 🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵
        item { SettingsSectionHeader("Cuenta") }

        item {
            val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            SettingsCard {
                SettingsInfoRow(
                    icon = Icons.Rounded.Person,
                    title = "Sesión iniciada como",
                    subtitle = user?.email ?: "Desconocido"
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            authViewModel.logout()
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Logout,
                        contentDescription = "Cerrar sesión",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = "Cerrar sesión",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // 🎵 SECCIÓN: ACERCA DE 🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵🎵
        item { SettingsSectionHeader("Acerca de") }

        item {
            SettingsCard {
                SettingsInfoRow(
                    icon = Icons.Rounded.MusicNote,
                    title = "Ghostymusicy",
                    subtitle = "Versión 1.0.0"
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                SettingsInfoRow(
                    icon = Icons.Rounded.Person,
                    title = "Desarrollador",
                    subtitle = "Diego Torrez"
                )
                // Additional rows removed
            }
        }

        if (userRole == "admin") {
            item { Spacer(Modifier.height(24.dp)) }
            item {
                Button(
                    onClick = { navController.navigate(Routes.ADMIN) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(Icons.Rounded.AdminPanelSettings, contentDescription = "Panel de Administrador", modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Panel de Administrador", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        item { Spacer(Modifier.height(100.dp)) } // Padding bottom
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun ThemeChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        tween(250)
    )
    val textColor by animateColorAsState(
        if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        tween(250)
    )
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = label, tint = textColor, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = textColor, fontSize = 11.sp)
    }
}

@Composable
private fun SettingsInfoRow(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SettingsSwitchRow(icon: ImageVector, title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
