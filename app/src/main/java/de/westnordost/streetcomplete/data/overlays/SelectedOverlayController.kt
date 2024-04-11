package de.westnordost.streetcomplete.data.overlays

import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.util.Listeners

class SelectedOverlayController(
    private val prefs: ObservableSettings,
    private val overlayRegistry: OverlayRegistry
) : SelectedOverlaySource {

    private val listeners = Listeners<SelectedOverlaySource.Listener>()

    override var selectedOverlay: Overlay?
        set(value) {
            if (selectedOverlay == value) return

            if (value != null && value in overlayRegistry) {
                prefs.putString(Prefs.SELECTED_OVERLAY, value.name)
            } else {

                prefs.remove(Prefs.SELECTED_OVERLAY)
            }
            listeners.forEach { it.onSelectedOverlayChanged() }
        }
        get() = prefs.getStringOrNull(Prefs.SELECTED_OVERLAY)?.let { overlayRegistry.getByName(it) }

    override fun addListener(listener: SelectedOverlaySource.Listener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: SelectedOverlaySource.Listener) {
        listeners.remove(listener)
    }
}
