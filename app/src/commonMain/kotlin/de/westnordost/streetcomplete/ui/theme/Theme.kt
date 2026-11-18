package de.westnordost.streetcomplete.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.preferences.Theme
import org.koin.compose.koinInject

@Composable
fun rememberAppTheme(preferences: Preferences = koinInject()): Theme {
    val theme = remember(preferences) { mutableStateOf(preferences.theme) }
    DisposableEffect(preferences) {
        val listener = preferences.onThemeChanged { theme.value = it }
        theme.value = preferences.theme
        onDispose { listener.deactivate() }
    }
    return theme.value
}

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
