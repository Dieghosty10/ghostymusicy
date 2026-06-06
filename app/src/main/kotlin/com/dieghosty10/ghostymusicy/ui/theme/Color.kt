package com.dieghosty10.ghostymusicy.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Paleta BASE (fondo/superficie — siempre oscuras en modo dark) ───────────
val BackgroundDark       = Color(0xFF080A0F)   // casi negro azulado
val SurfaceDark          = Color(0xFF111318)
val SurfaceVariantDark   = Color(0xFF1C1F27)

val BackgroundLight      = Color(0xFFF5F7FF)
val SurfaceLight         = Color(0xFFFFFFFF)
val SurfaceVariantLight  = Color(0xFFE8EAED)

// ─── Textos ───────────────────────────────────────────────────────────────────
val TextPrimary          = Color(0xFFF0F2FA)
val TextSecondary        = Color(0xFF8B90A4)
val TextPrimaryLight     = Color(0xFF0D1117)
val TextSecondaryLight   = Color(0xFF6B7280)

// ─── Estado ───────────────────────────────────────────────────────────────────
val ErrorRed   = Color(0xFFEF4444)
val SuccessGreen = Color(0xFF22C55E)

// ─── Acentos disponibles (Primary) ───────────────────────────────────────────
/** Azul eléctrico — default */
val AccentBlue     = Color(0xFF3B82F6)
/** Violeta vibrante */
val AccentPurple   = Color(0xFF8B5CF6)
/** Verde neón */
val AccentGreen    = Color(0xFF10B981)
/** Naranja fuego */
val AccentOrange   = Color(0xFFF97316)
/** Rosa neón */
val AccentRose     = Color(0xFFEC4899)
/** Cian tecnológico */
val AccentCyan     = Color(0xFF06B6D4)
/** Rojo intenso */
val AccentRed      = Color(0xFFEF4444)
/** Ámbar */
val AccentAmber    = Color(0xFFF59E0B)

enum class GhostyAccent(val color: Color, val label: String) {
    BLUE   (AccentBlue,   "Azul"),
    PURPLE (AccentPurple, "Violeta"),
    GREEN  (AccentGreen,  "Verde"),
    ORANGE (AccentOrange, "Naranja"),
    ROSE   (AccentRose,   "Rosa"),
    CYAN   (AccentCyan,   "Cian"),
    RED    (AccentRed,    "Rojo"),
    AMBER  (AccentAmber,  "Ámbar"),
}

/** Convierte el nombre almacenado en DataStore → Color primario */
fun accentFromName(name: String): Color =
    GhostyAccent.entries.firstOrNull { it.name == name }?.color ?: AccentBlue
