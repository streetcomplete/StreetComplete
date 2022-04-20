package de.westnordost.streetcomplete.data.overlay

import de.westnordost.streetcomplete.overlays.Overlay

class SelectedOverlayController(
    private val selectedOverlayStore: SelectedOverlayStore,
    private val overlayRegistry: OverlayRegistry
) {
    var selectedOverlay: Overlay?
    set(value) {
        if (value != null && value in overlayRegistry) {
            selectedOverlayStore.set(value::class.simpleName!!)
        }
    }
    get() = selectedOverlayStore.get()?.let { overlayRegistry.getByName(it) }
}
