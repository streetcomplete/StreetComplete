package de.westnordost.streetcomplete.data.overlays

interface SelectedOverlaySource {
    interface Listener {
        fun onSelectedOverlayChanged()
    }

    val selectedOverlay: Overlay?

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
