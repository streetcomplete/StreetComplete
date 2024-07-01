package de.westnordost.streetcomplete.data.overlays

import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.util.Listeners

class SelectedOverlayController(
    private val prefs: Preferences,
    private val overlayRegistry: OverlayRegistry
) : SelectedOverlaySource {

    private val listeners = Listeners<SelectedOverlaySource.Listener>()

    override var selectedOverlay: Overlay?
        set(value) {
            if (selectedOverlay == value) return

            if (value != null && value in overlayRegistry) {
                prefs.selectedOverlayName = value.name
            } else {
                prefs.selectedOverlayName = null
            }
            // TODO hmm....
            listeners.forEach { it.onSelectedOverlayChanged() }
        }
        get() = prefs.selectedOverlayName?.let { overlayRegistry.getByName(it) }

    override fun addListener(listener: SelectedOverlaySource.Listener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: SelectedOverlaySource.Listener) {
        listeners.remove(listener)
    }
}
