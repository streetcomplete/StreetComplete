package de.westnordost.streetcomplete.osm.lane_narrowing_traffic_calming

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.osm.lane_narrowing_traffic_calming.LaneNarrowingTrafficCalming.*

/** Puts the new lane narrowing traffic calming type into tags */
fun LaneNarrowingTrafficCalming.applyTo(tags: Tags) {
    val values = tags["traffic_calming"]
        ?.let { expandTrafficCalmingValue(it) }.orEmpty()
        .toMutableList()

    // values we will overwrite must be removed first
    values.removeAll(listOf("choker", "island", "chicane"))
    // rather add to front than to back, because road narrowing are the more prominent form of
    // traffic calming than rumbling strips or whatever
    values.addAll(0, when (this) {
        CHOKER -> listOf("choker")
        ISLAND -> listOf("island")
        CHICANE -> listOf("chicane")
        CHOKED_ISLAND -> listOf("choker", "island")
    })

    val newTagValue = values.joinToString(";")
    // prefer semicolon-tagging over conjoined tags (i.e. leave the tag in expanded form)
    tags.updateWithCheckDate("traffic_calming", newTagValue)
}

// TODO tests
