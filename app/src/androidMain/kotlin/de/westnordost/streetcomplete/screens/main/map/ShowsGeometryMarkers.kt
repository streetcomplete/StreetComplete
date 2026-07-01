package de.westnordost.streetcomplete.screens.main.map

import androidx.annotation.DrawableRes
import androidx.annotation.UiThread
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import org.jetbrains.compose.resources.DrawableResource

interface ShowsGeometryMarkers {
    fun putMarkersForCurrentHighlighting(markers: Iterable<Marker>)
    @UiThread fun deleteMarkerForCurrentHighlighting(geometry: ElementGeometry)
    @UiThread fun clearMarkersForCurrentHighlighting()
}

data class Marker(
    val geometry: ElementGeometry,
    val icon: DrawableResource? = null,
    val title: String? = null
)
