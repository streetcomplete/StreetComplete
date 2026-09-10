package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.ui.common.quest.Marker
import org.jetbrains.compose.resources.DrawableResource

/** Transient selection and visibility state rendered on top of the durable main map. */
@Stable
internal class MainMapContentState {
    var highlightedGeometry by mutableStateOf<ElementGeometry?>(null)
        private set
    var markers by mutableStateOf<List<Marker>>(emptyList())
        private set
    var selectedPins by mutableStateOf<SelectedMapPins?>(null)
        private set
    var pinMode by mutableStateOf(MainMapPinMode.QUESTS)
        private set
    var showPins by mutableStateOf(true)
        private set
    var showStyleableOverlay by mutableStateOf(true)
        private set

    fun showGeometry(geometry: ElementGeometry) {
        highlightedGeometry = geometry
    }

    fun setMarkers(markers: Iterable<Marker>) {
        this.markers = markers.toList()
    }

    fun selectPins(icon: DrawableResource, positions: Collection<LatLon>) {
        selectedPins = SelectedMapPins(icon, positions.toList())
    }

    fun updatePinMode(mode: MainMapPinMode) {
        pinMode = mode
    }

    fun hidePins() {
        showPins = false
    }

    fun hideOverlay() {
        showStyleableOverlay = false
    }

    fun clearSelectedPins() {
        selectedPins = null
    }

    fun clearHighlighting() {
        highlightedGeometry = null
        markers = emptyList()
        selectedPins = null
        showPins = true
        showStyleableOverlay = true
    }
}

enum class MainMapPinMode { NONE, QUESTS, EDITS }
