package de.westnordost.streetcomplete.osm.street_parking

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.expandSides
import de.westnordost.streetcomplete.osm.hasCheckDateForKey
import de.westnordost.streetcomplete.osm.mergeSides
import de.westnordost.streetcomplete.osm.updateCheckDateForKey

fun LeftAndRightStreetParking.applyTo(tags: Tags) {
    if (left == null && right == null) return
    /* for being able to modify only one side (e.g. `left` is null while `right` is not null),
       the sides conflated in `:both` keys need to be separated first. E.g. `parking:lane=no`
       when left side is made `separate´ should become
       - parking:lane:right=no
       - parking:lane:left=separate
       First separating the values and then later conflating them again, if possible, solves this.
     */
    tags.expandSides("parking:lane")
    ParkingOrientation.values().forEach { tags.expandSides("parking:lane", it.osmValue, true) }

    // parking:lane:<left/right>
    left?.applyTo(tags, "left")
    right?.applyTo(tags, "right")

    tags.mergeSides("parking:lane")
    ParkingOrientation.values().forEach { tags.mergeSides("parking:lane", it.osmValue) }

    if (!tags.hasChanges || tags.hasCheckDateForKey("parking:lane")) {
        tags.updateCheckDateForKey("parking:lane")
    }
}

private fun StreetParking.applyTo(tags: Tags, side: String) {
    tags["parking:lane:$side"] = osmLaneValue
    // clear previous orientation(s)
    ParkingOrientation.values().forEach { tags.remove("parking:lane:$side:${it.osmValue}") }
    if (this is StreetParkingPositionAndOrientation) {
        tags["parking:lane:$side:$osmLaneValue"] = position.osmValue
    }
}

/** get the OSM value for the parking:lane key */
private val StreetParking.osmLaneValue get() = when (this) {
    is StreetParkingPositionAndOrientation -> orientation.osmValue
    NoStreetParking -> "no"
    StreetParkingSeparate -> "separate"
    UnknownStreetParking, IncompleteStreetParking -> throw IllegalArgumentException("Attempting to tag invalid parking lane")
}

private val ParkingPosition.osmValue get() = when (this) {
    ParkingPosition.ON_STREET ->         "on_street"
    ParkingPosition.HALF_ON_KERB ->      "half_on_kerb"
    ParkingPosition.ON_KERB ->           "on_kerb"
    ParkingPosition.STREET_SIDE ->       "street_side"
    ParkingPosition.PAINTED_AREA_ONLY -> "painted_area_only"
}

private val ParkingOrientation.osmValue get() = when (this) {
    ParkingOrientation.PARALLEL ->      "parallel"
    ParkingOrientation.DIAGONAL ->      "diagonal"
    ParkingOrientation.PERPENDICULAR -> "perpendicular"
}
