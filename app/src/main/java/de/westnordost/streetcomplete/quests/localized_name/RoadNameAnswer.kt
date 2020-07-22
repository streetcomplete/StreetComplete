package de.westnordost.streetcomplete.quests.localized_name

import de.westnordost.osmapi.map.data.LatLon

sealed class RoadNameAnswer

data class RoadName(
    val localizedNames: List<LocalizedName>,
    val wayId: Long, val wayGeometry: List<LatLon>)
    : RoadNameAnswer()

object NoRoadName : RoadNameAnswer()
object RoadIsServiceRoad : RoadNameAnswer()
object RoadIsTrack : RoadNameAnswer()
object RoadIsLinkRoad : RoadNameAnswer()
