package de.westnordost.streetcomplete.quests.road_name

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.osm.LocalizedName

sealed interface RoadNameAnswer

data class RoadName(
    val localizedNames: List<LocalizedName>,
    val wayId: Long,
    val wayGeometry: List<LatLon>
) : RoadNameAnswer

object NoRoadName : RoadNameAnswer
object RoadIsServiceRoad : RoadNameAnswer
object RoadIsTrack : RoadNameAnswer
object RoadIsLinkRoad : RoadNameAnswer
