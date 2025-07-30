package de.westnordost.streetcomplete.ui.ktx

import de.westnordost.streetcomplete.resources.Res

actual fun Res.exists(path: String): Boolean {
    try {
        getUri(path)
        return true
    } catch (_: Exception) {
        return false
    }
}
