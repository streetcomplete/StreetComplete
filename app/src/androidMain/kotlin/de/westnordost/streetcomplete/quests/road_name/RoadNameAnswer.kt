package de.westnordost.streetcomplete.quests.road_name

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.osm.LocalizedName

sealed interface RoadNameAnswer

data class RoadName(
    val localizedNames: List<LocalizedName>,
    val wayId: Long,
    val wayGeometry: List<LatLon>
) : RoadNameAnswer

data object NoRoadName : RoadNameAnswer
data object RoadIsServiceRoad : RoadNameAnswer
data object RoadIsTrack : RoadNameAnswer
data object RoadIsLinkRoad : RoadNameAnswer
