package de.westnordost.streetcomplete.quests

import androidx.annotation.DrawableRes
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry

interface ShowsGeometryMarkers {
    fun putMarkerForCurrentQuest(geometry: ElementGeometry, @DrawableRes drawableResId: Int?)
    fun deleteMarkerForCurrentQuest(geometry: ElementGeometry)
}
