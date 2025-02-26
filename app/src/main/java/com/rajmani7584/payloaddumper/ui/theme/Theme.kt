package com.rajmani7584.payloaddumper.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF000000),
    primaryContainer = Color(0xFFD7D8D8),
    onPrimaryContainer = Color.Black,
    onBackground = Color(0xFFD7D8D8),
    surface = Color(0xFF303030),
    onError = Color(0xFFD7D8D8),
    errorContainer = Color(0xFFD71921),
    onErrorContainer = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    primaryContainer = Color(0xFF2E3235),
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color(0xFFF9F9F9),
    surface = Color(0xFFD1D1D1),
    surfaceContainer = Color(0xFFEEEEEE),
    onPrimaryContainer = Color.White,
    errorContainer = Color(0xFFD71921),
    onErrorContainer = Color.White
    /* Other default colors to override
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFD1D1D1),
    onTertiary = Color.White,
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun PayloadDumperAndroidTheme(
    darkTheme: Boolean,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
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