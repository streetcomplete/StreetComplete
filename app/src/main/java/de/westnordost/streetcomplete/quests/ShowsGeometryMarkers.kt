package de.westnordost.streetcomplete.quests

import androidx.annotation.DrawableRes
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry

interface ShowsGeometryMarkers {
    fun putMarkerForCurrentHighlighting(
        geometry: ElementGeometry,
        @DrawableRes drawableResId: Int?,
        title: String?
    )
    fun deleteMarkerForCurrentHighlighting(geometry: ElementGeometry)

    fun clearMarkersForCurrentHighlighting()
}
