package de.westnordost.streetcomplete.quests.oneway

import de.westnordost.osmapi.map.data.LatLon

data class TrafficFlowSegment(val fromPosition: LatLon, val toPosition: LatLon)
