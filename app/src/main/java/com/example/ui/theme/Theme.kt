package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
  primary = AmberLight,
  onPrimary = Color.Black,
  primaryContainer = AmberDark,
  onPrimaryContainer = Color.White,
  secondary = MintGreenLight,
  onSecondary = Color.Black,
  tertiary = DeliveryBlueLight,
  onTertiary = Color.Black,
  background = DarkBackground,
  onBackground = DarkTextPrimary,
  surface = DarkSurface,
  onSurface = DarkTextPrimary,
  surfaceVariant = DarkSurfaceCard,
  onSurfaceVariant = DarkTextSecondary
)

private val LightColorScheme = lightColorScheme(
  primary = AmberPrimary,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFFFE0B2),
  onPrimaryContainer = AmberDark,
  secondary = MintGreen,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFE0F2F1),
  onSecondaryContainer = MintGreenDark,
  tertiary = DeliveryBlue,
  onTertiary = Color.White,
  background = SurfaceLight,
  onBackground = TextPrimaryLight,
  surface = SurfaceCardLight,
  onSurface = TextPrimaryLight,
  surfaceVariant = Color(0xFFF0F2F5),
  onSurfaceVariant = TextSecondaryLight
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
