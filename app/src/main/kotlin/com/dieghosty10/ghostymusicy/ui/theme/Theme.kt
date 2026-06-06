package com.dieghosty10.ghostymusicy.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.dieghosty10.ghostymusicy.constants.DarkModeKey
import com.dieghosty10.ghostymusicy.constants.CustomThemeColorKey
import com.dieghosty10.ghostymusicy.utils.rememberEnumPreference
import com.dieghosty10.ghostymusicy.utils.rememberPreference

enum class DarkMode { ON, OFF, FOLLOW_SYSTEM }

@Composable
fun buildColorScheme(dark: Boolean, accent: Color) = if (dark) {
    darkColorScheme(
        primary             = accent,
        secondary           = accent.copy(red = accent.red * 0.8f, green = accent.green * 0.8f, blue = accent.blue * 0.8f),
        tertiary            = AccentCyan,
        background          = BackgroundDark,
        surface             = SurfaceDark,
        surfaceVariant      = SurfaceVariantDark,
        onPrimary           = TextPrimary,
        onSecondary         = TextPrimary,
        onTertiary          = TextPrimary,
        onBackground        = TextPrimary,
        onSurface           = TextPrimary,
        onSurfaceVariant    = TextSecondary,
        error               = ErrorRed,
        onError             = TextPrimary,
    )
} else {
    lightColorScheme(
        primary             = accent,
        secondary           = accent.copy(alpha = 0.8f),
        tertiary            = AccentCyan,
        background          = BackgroundLight,
        surface             = SurfaceLight,
        surfaceVariant      = SurfaceVariantLight,
        onPrimary           = Color.White,
        onSecondary         = Color.White,
        onTertiary          = Color.White,
        onBackground        = TextPrimaryLight,
        onSurface           = TextPrimaryLight,
        onSurfaceVariant    = TextSecondaryLight,
        error               = ErrorRed,
        onError             = Color.White,
    )
}

@Composable
fun ghostymusicyTheme(
    content: @Composable () -> Unit,
) {
    val darkModeState by rememberEnumPreference(DarkModeKey, DarkMode.ON)
    val accentName    by rememberPreference(CustomThemeColorKey, GhostyAccent.BLUE.name)

    val systemDark = isSystemInDarkTheme()
    val isDark = when (darkModeState) {
        DarkMode.ON            -> true
        DarkMode.OFF           -> false
        DarkMode.FOLLOW_SYSTEM -> systemDark
    }

    val accent = accentFromName(accentName)
    val colorScheme = buildColorScheme(isDark, accent)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor     = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars     = !isDark
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content,
    )
}
