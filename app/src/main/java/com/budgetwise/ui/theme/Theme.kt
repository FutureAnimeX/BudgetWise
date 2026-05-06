package com.budgetwise.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val TealPrimary    = Color(0xFF028090)
val TealLight      = Color(0xFF02C39A)
val NavyBackground = Color(0xFF0A1628)
val CardSurface    = Color(0xFF0D2137)
val CardSurface2   = Color(0xFF112840)
val DangerRed      = Color(0xFFF96167)
val MutedGray      = Color(0xFF94A3B8)

private val DarkColorScheme = darkColorScheme(
    primary            = TealPrimary,
    onPrimary          = Color.White,
    primaryContainer   = CardSurface2,
    onPrimaryContainer = TealLight,
    secondary          = TealLight,
    onSecondary        = NavyBackground,
    background         = NavyBackground,
    onBackground       = Color(0xFFF0F8FF),
    surface            = CardSurface,
    onSurface          = Color(0xFFF0F8FF),
    surfaceVariant     = CardSurface2,
    onSurfaceVariant   = MutedGray,
    error              = DangerRed,
    outline            = Color(0xFF334155)
)

private val LightColorScheme = lightColorScheme(
    primary    = TealPrimary,
    onPrimary  = Color.White,
    secondary  = TealLight,
    background = Color(0xFFF8FAFC),
    surface    = Color.White,
    error      = DangerRed
)

@Composable
fun BudgetWiseTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content
    )
}
