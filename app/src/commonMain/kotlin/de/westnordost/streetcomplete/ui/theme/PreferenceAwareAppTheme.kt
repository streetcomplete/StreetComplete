package de.westnordost.streetcomplete.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.preferences.Theme
import org.koin.compose.koinInject

/** Applies the persisted display settings at the shared UI root on every target. */
@Composable
fun PreferenceAwareAppTheme(
    preferences: Preferences = koinInject(),
    content: @Composable () -> Unit,
) {
    var theme by remember(preferences) { mutableStateOf(preferences.theme) }
    var language by remember(preferences) { mutableStateOf(preferences.language) }
    var keepScreenOn by remember(preferences) { mutableStateOf(preferences.keepScreenOn) }

    DisposableEffect(preferences) {
        val listeners = listOf(
            preferences.onThemeChanged { theme = it },
            preferences.onLanguageChanged { language = it },
            preferences.onKeepScreenOnChanged { keepScreenOn = it },
        )
        onDispose { listeners.forEach { it.deactivate() } }
    }

    PlatformKeepScreenOnEffect(keepScreenOn)
    ApplyApplicationLanguageEffect(language)

    val darkTheme = when (theme) {
        Theme.SYSTEM -> isSystemInDarkTheme()
        Theme.LIGHT -> false
        Theme.DARK -> true
    }
    AppTheme(darkTheme) {
        // Recreate resource consumers after the platform locale has changed.
        key(language) { content() }
    }
}

@Composable
internal expect fun PlatformKeepScreenOnEffect(enabled: Boolean)

@Composable
internal expect fun ApplyApplicationLanguageEffect(language: String?)
