package de.westnordost.streetcomplete.data.overlay

import de.westnordost.streetcomplete.overlays.Overlay

interface SelectedOverlaySource {
    interface Listener {
        fun onSelectedOverlayChanged()
    }

    val selectedOverlay: Overlay?

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
