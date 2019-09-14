package de.westnordost.streetcomplete.quests.localized_name

import de.westnordost.streetcomplete.data.osm.ElementPolylinesGeometry

sealed class RoadNameAnswer

data class RoadName(
    val localizedNames: List<LocalizedName>,
    val wayId: Long, val wayGeometry: ElementPolylinesGeometry)
    : RoadNameAnswer()

object NoRoadName : RoadNameAnswer()
object RoadIsServiceRoad : RoadNameAnswer()
object RoadIsTrack : RoadNameAnswer()
object RoadIsLinkRoad : RoadNameAnswer()
