package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val WhiteLightColorScheme = lightColorScheme(
  primary = LightPrimary,
  onPrimary = LightOnPrimary,
  primaryContainer = LightPrimaryContainer,
  onPrimaryContainer = LightOnPrimaryContainer,
  secondary = LightSecondary,
  onSecondary = LightOnSecondary,
  secondaryContainer = LightSecondaryContainer,
  onSecondaryContainer = LightOnSecondaryContainer,
  tertiary = LightTertiary,
  onTertiary = LightOnTertiary,
  tertiaryContainer = LightTertiaryContainer,
  onTertiaryContainer = LightOnTertiaryContainer,
  background = LightBackground,
  onBackground = LightOnBackground,
  surface = LightSurface,
  onSurface = LightOnSurface,
  surfaceVariant = LightSurfaceVariant,
  onSurfaceVariant = LightOnSurfaceVariant,
  outline = LightOutline,
  outlineVariant = LightOutlineVariant
)

@Composable
fun PacketCaptureTheme(
  darkTheme: Boolean = false, // Enforce crisp white theme as requested by user
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = WhiteLightColorScheme,
    typography = Typography,
    content = content
  )
}
