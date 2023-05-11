package de.westnordost.streetcomplete.screens.main.map

import androidx.annotation.DrawableRes
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry

interface ShowsGeometryMarkers {
    fun putMarkerForCurrentHighlighting(
        geometry: ElementGeometry,
        @DrawableRes drawableResId: Int?,
        title: String?,
        color: Int? = null,
        rotation: Double? = null
    )
    fun deleteMarkerForCurrentHighlighting(geometry: ElementGeometry)

    fun clearMarkersForCurrentHighlighting()
}
