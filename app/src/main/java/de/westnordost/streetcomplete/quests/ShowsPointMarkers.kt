package de.westnordost.streetcomplete.quests

import androidx.annotation.DrawableRes
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon

interface ShowsPointMarkers {
    fun putMarkerForCurrentQuest(pos: LatLon, @DrawableRes drawableResId: Int)
    fun deleteMarkerForCurrentQuest(pos: LatLon)
}
