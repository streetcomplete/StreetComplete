package de.westnordost.streetcomplete.screens.settings.presets

import androidx.compose.ui.platform.Clipboard

internal expect suspend fun Clipboard.setPlainText(text: String)
