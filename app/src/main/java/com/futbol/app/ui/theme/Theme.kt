package com.futbol.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary              = Blue700,
    onPrimary            = White,
    primaryContainer     = Blue100,
    onPrimaryContainer   = Blue900,
    secondary            = Blue400,
    onSecondary          = White,
    secondaryContainer   = Blue200,
    onSecondaryContainer = Blue800,
    tertiary             = LightBlue500,
    onTertiary           = White,
    background           = OffWhite,
    onBackground         = DarkText,
    surface              = White,
    onSurface            = DarkText,
    surfaceVariant       = SurfaceGray,
    onSurfaceVariant     = TextGray,
    outline              = OutlineGray,
    error                = ErrorRed,
    onError              = White,
    errorContainer       = ErrorLight,
    onErrorContainer     = ErrorRed
)

private val DarkColors = darkColorScheme(
    primary              = Color(0xFFF48FB1),
    onPrimary            = Color(0xFF4D0029),
    primaryContainer     = Color(0xFF880E4F),
    onPrimaryContainer   = Color(0xFFFFD9E2),
    secondary            = Color(0xFFFCE4EC),
    onSecondary          = Color(0xFF3B0019),
    secondaryContainer   = Color(0xFF702D46),
    onSecondaryContainer = Color(0xFFFFD9E2),
    background           = Color(0xFF201A1B),
    onBackground         = Color(0xFFECE0E1),
    surface              = Color(0xFF1A1113),
    onSurface            = Color(0xFFECE0E1),
    surfaceVariant       = Color(0xFF352226),
    onSurfaceVariant     = Color(0xFFF0BFC8),
    outline              = Color(0xFF9E8C90),
    error                = Color(0xFFFFB4AB),
    onError              = Color(0xFF690005),
    errorContainer       = Color(0xFF93000A),
    onErrorContainer     = Color(0xFFFFDAD6)
)

@Composable
fun FutbolAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        content     = content
    )
}
