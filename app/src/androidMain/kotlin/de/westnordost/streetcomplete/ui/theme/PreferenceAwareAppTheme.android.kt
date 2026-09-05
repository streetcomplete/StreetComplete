package de.westnordost.streetcomplete.ui.theme

import androidx.compose.runtime.Composable

@Composable
internal actual fun PlatformKeepScreenOnEffect(enabled: Boolean) {
    // MainActivity owns the Android window flag and refreshes it whenever the main route is shown.
}

@Composable
internal actual fun ApplyApplicationLanguageEffect(language: String?) {
    // MainActivity recreates with the selected locale through its localized base context.
}
