package de.westnordost.streetcomplete.screens.main.map

import androidx.annotation.DrawableRes
import androidx.annotation.UiThread
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry

interface ShowsGeometryMarkers {
    fun putMarkersForCurrentHighlighting(markers: Iterable<Marker>)
    @UiThread fun deleteMarkerForCurrentHighlighting(geometry: ElementGeometry)
    @UiThread fun clearMarkersForCurrentHighlighting()
}

data class Marker(
    val geometry: ElementGeometry,
    @DrawableRes val icon: Int? = null,
    val title: String? = null
)
