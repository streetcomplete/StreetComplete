package de.westnordost.streetcomplete.quests.oneway_suspects.data

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import kotlinx.serialization.Serializable

@Serializable
data class TrafficFlowSegment(
    val wayId: Long,
    val fromPosition: LatLon,
    val toPosition: LatLon,
)
