package de.westnordost.streetcomplete.ui.util

import androidx.compose.runtime.staticCompositionLocalOf

private fun noLocalProvidedFor(name: String): Nothing {
    error("CompositionLocal $name not present")
}
