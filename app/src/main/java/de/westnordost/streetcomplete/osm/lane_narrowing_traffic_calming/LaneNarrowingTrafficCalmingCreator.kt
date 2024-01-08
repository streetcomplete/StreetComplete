package de.westnordost.streetcomplete.osm.lane_narrowing_traffic_calming

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.lane_narrowing_traffic_calming.LaneNarrowingTrafficCalming.*
import de.westnordost.streetcomplete.osm.removeCheckDatesForKey
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.util.ktx.toYesNo

/** Puts the new lane narrowing traffic calming type into tags or removes it if null */
fun LaneNarrowingTrafficCalming?.applyTo(tags: Tags) {
    val currentValues = tags["traffic_calming"]
        ?.let { expandTrafficCalmingValue(it) }.orEmpty()

    val values = currentValues.toMutableList()
    // values we will overwrite must be removed first
    values.removeAll(listOf("choker", "island", "chicane"))
    // rather add to front than to back, because road narrowing are the more prominent form of
    // traffic calming than rumbling strips or whatever
    values.addAll(0, when (this) {
        CHOKER -> listOf("choker")
        ISLAND -> listOf("island")
        CHICANE -> listOf("chicane")
        CHOKED_ISLAND -> listOf("choker", "island")
        null -> listOf()
    })

    // update crossing:island if it is a crossing
    val isCrossing = tags["highway"] == "crossing"
    if (isCrossing) {
        // in any case, clean deprecated tag
        if (tags["crossing"] == "island") tags.remove("crossing")

        tags["crossing:island"] = ("island" in values).toYesNo()
    }

    if (values.isEmpty()) {
        tags.remove("traffic_calming")
        tags.removeCheckDatesForKey("traffic_calming")
    } else {
        // prefer semicolon-tagging over conjoined tags (i.e. prefer choker;island over choked_island)
        tags.updateWithCheckDate("traffic_calming", values.joinToString(";"))
    }
}
