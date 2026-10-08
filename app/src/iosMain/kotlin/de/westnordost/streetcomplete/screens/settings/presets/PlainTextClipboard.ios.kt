package de.westnordost.streetcomplete.screens.settings.presets

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard

@OptIn(ExperimentalComposeUiApi::class)
internal actual suspend fun Clipboard.setPlainText(text: String) {
    setClipEntry(ClipEntry.withPlainText(text))
}
