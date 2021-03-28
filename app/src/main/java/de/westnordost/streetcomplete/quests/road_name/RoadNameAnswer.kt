package de.westnordost.streetcomplete.quests.road_name

import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.quests.LocalizedName

sealed class RoadNameAnswer

data class RoadName(
    val localizedNames: List<LocalizedName>,
    val wayId: Long, val wayGeometry: List<LatLon>)
    : RoadNameAnswer()

object NoRoadName : RoadNameAnswer()
object RoadIsServiceRoad : RoadNameAnswer()
object RoadIsTrack : RoadNameAnswer()
object RoadIsLinkRoad : RoadNameAnswer()
