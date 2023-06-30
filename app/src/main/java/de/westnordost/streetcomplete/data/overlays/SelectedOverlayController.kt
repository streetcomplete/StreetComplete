package de.westnordost.streetcomplete.data.overlays

import de.westnordost.streetcomplete.overlays.Overlay
import java.util.concurrent.CopyOnWriteArrayList

class SelectedOverlayController(
    private val selectedOverlayStore: SelectedOverlayStore,
    private val overlayRegistry: OverlayRegistry
) : SelectedOverlaySource {

    private val listeners = CopyOnWriteArrayList<SelectedOverlaySource.Listener>()

    override var selectedOverlay: Overlay?
    set(value) {
        if (selectedOverlay == value) return

        if (value != null && value in overlayRegistry) {
            selectedOverlayStore.set(value.name)
        } else {
            selectedOverlayStore.set(null)
        }
        listeners.forEach { it.onSelectedOverlayChanged() }
    }
    get() = selectedOverlayStore.get()?.let { overlayRegistry.getByName(it) }

    override fun addListener(listener: SelectedOverlaySource.Listener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: SelectedOverlaySource.Listener) {
        listeners.remove(listener)
    }
}
