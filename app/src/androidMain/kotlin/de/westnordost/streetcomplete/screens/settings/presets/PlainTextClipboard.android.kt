package de.westnordost.streetcomplete.screens.settings.presets

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard

internal actual suspend fun Clipboard.setPlainText(text: String) {
    setClipEntry(ClipEntry(ClipData.newPlainText(null, text)))
}
