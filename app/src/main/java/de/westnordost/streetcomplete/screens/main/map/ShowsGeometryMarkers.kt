package de.westnordost.streetcomplete.screens.main.map

import androidx.annotation.DrawableRes
import androidx.annotation.UiThread
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry

interface ShowsGeometryMarkers {
    @UiThread fun putMarkerForCurrentHighlighting(
        geometry: ElementGeometry,
        @DrawableRes drawableResId: Int?,
        title: String?
    )
    @UiThread fun deleteMarkerForCurrentHighlighting(geometry: ElementGeometry)

    @UiThread fun clearMarkersForCurrentHighlighting()
}
