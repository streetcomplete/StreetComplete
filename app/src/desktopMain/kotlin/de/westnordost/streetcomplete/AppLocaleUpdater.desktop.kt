package de.westnordost.streetcomplete

import androidx.compose.ui.text.intl.Locale

private val systemLocale = Locale(java.util.Locale.getDefault().toLanguageTag())

internal actual fun applyAppLocale(locale: Locale?) {
    java.util.Locale.setDefault(resolveAppLocale(locale, listOf(systemLocale)).platformLocale)
}
