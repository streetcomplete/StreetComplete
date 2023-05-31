package de.westnordost.streetcomplete.osm.street_parking

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.expandSides
import de.westnordost.streetcomplete.osm.hasCheckDateForKey
import de.westnordost.streetcomplete.osm.mergeSides
import de.westnordost.streetcomplete.osm.removeCheckDatesForKey
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.*
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.*
import de.westnordost.streetcomplete.osm.updateCheckDateForKey

fun LeftAndRightStreetParking.applyTo(tags: Tags) {
    if (left == null && right == null) return
    /* for being able to modify only one side (e.g. `left` is null while `right` is not null),
       the sides conflated in `:both` keys need to be separated first. E.g. `parking:both=no`
       when left side is made `separate´ should become
       - parking:right=no
       - parking:left=separate
       First separating the values and then later conflating them again, if possible, solves this.
     */
    tags.expandSides("parking", includeBareTag = false)
    tags.expandSides("parking", "orientation", includeBareTag = false)
    tags.expandSides("parking", "markings", includeBareTag = false)
    tags.expandSides("parking", "staggered", includeBareTag = false)

    // parking:<left/right>
    left?.applyTo(tags, "left")
    right?.applyTo(tags, "right")

    tags.mergeSides("parking")
    tags.mergeSides("parking", "orientation")
    tags.mergeSides("parking", "markings")
    tags.mergeSides("parking", "staggered")

    if (!tags.hasChanges || tags.hasCheckDateForKey("parking")) {
        tags.updateCheckDateForKey("parking")
    }

    for (side in listOf(":left", ":right", ":both", "")) {
        tags.remove("parking:lane$side")
        tags.remove("parking:lane$side:perpendicular")
        tags.remove("parking:lane$side:diagonal")
        tags.remove("parking:lane$side:parallel")
    }
    tags.removeCheckDatesForKey("parking:lane")
}

private fun StreetParking.applyTo(tags: Tags, side: String) {
    tags["parking:$side"] =
        // "shoulder" as value is not supported but instead interpreted as "off street", i.e. the
        // same as "on_kerb". However, since "shoulder" is an approved value, it will not be over-
        // written by StreetComplete if "adjacent to street" is selected
        // https://wiki.openstreetmap.org/wiki/Talk:Street_parking#Suggestion_to_remove_parking:side=shoulder
        if (osmPositionValue == "on_kerb" && tags["parking:$side"] == "shoulder") "shoulder"
        else osmPositionValue

    if (this is StreetParkingPositionAndOrientation) {
        tags["parking:$side:orientation"] = orientation.osmValue
        if (position == PAINTED_AREA_ONLY) {
            tags["parking:$side:markings"] = "yes"
        }
        if (position.isStaggered) {
            tags["parking:$side:staggered"] = "yes"
        } else {
            tags.remove("parking:$side:staggered")
        }
    } else {
        tags.remove("parking:$side:orientation")
        tags.remove("parking:$side:markings")
        tags.remove("parking:$side:staggered")
    }
}

/** get the OSM value for the parking:<side> key */
private val StreetParking.osmPositionValue get() = when (this) {
    is StreetParkingPositionAndOrientation -> position.osmValue
    NoStreetParking -> "no"
    StreetParkingSeparate -> "separate"
    UnknownStreetParking, IncompleteStreetParking -> throw IllegalArgumentException("Attempting to tag invalid parking lane")
}

private val ParkingPosition.osmValue get() = when (this) {
    ON_STREET, STAGGERED_ON_STREET, PAINTED_AREA_ONLY ->
        "lane"
    HALF_ON_STREET, STAGGERED_HALF_ON_STREET ->
        "half_on_kerb"
    OFF_STREET ->
        "on_kerb"
    STREET_SIDE ->
        "street_side"
}

private val ParkingOrientation.osmValue get() = when (this) {
    PARALLEL ->      "parallel"
    DIAGONAL ->      "diagonal"
    PERPENDICULAR -> "perpendicular"
}
