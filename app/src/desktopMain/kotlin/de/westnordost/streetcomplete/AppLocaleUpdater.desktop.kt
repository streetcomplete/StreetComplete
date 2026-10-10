package de.westnordost.streetcomplete

import androidx.compose.ui.text.intl.Locale

private val systemLocale = java.util.Locale.getDefault()

internal actual fun applyAppLocale(locale: Locale?) {
    java.util.Locale.setDefault(locale?.platformLocale ?: systemLocale)
}
