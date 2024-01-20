package de.westnordost.streetcomplete.data.overlays

import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.util.prefs.Preferences

class SelectedOverlayStore(private val prefs: Preferences) {

    fun get(): String? = prefs.getStringOrNull(Prefs.SELECTED_OVERLAY)

    fun set(value: String?) {
        prefs.putString(Prefs.SELECTED_OVERLAY, value)
    }
}
