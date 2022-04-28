package de.westnordost.streetcomplete.quests

import androidx.annotation.DrawableRes
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry

interface ShowsGeometryMarkers {
    fun putMarkerForCurrentFocus(
        geometry: ElementGeometry,
        @DrawableRes drawableResId: Int?,
        title: String?
    )
    fun deleteMarkerForCurrentFocus(geometry: ElementGeometry)

    fun clearMarkersForCurrentFocus()
}
