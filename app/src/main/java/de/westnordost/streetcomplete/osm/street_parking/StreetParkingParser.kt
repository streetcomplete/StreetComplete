package de.westnordost.streetcomplete.osm.street_parking

import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.DIAGONAL
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.PARALLEL
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.PERPENDICULAR
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.HALF_ON_KERB
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.ON_KERB
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.ON_STREET
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.PAINTED_AREA_ONLY
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.STREET_SIDE

fun createStreetParkingSides(tags: Map<String, String>): LeftAndRightStreetParking? {
    val expandedTags = expandRelevantSidesTags(tags)
    // then get the values for left and right
    val left = createParkingForSide(expandedTags, "left")
    val right = createParkingForSide(expandedTags, "right")

    if (left == null && right == null) return null

    return LeftAndRightStreetParking(left, right)
}

private fun createParkingForSide(tags: Map<String, String>, side: String?): StreetParking? {
    val sideVal = if (side != null) ":$side" else ""

    val parkingValue = tags["parking:lane$sideVal"] ?: return null

    when (parkingValue) {
        "no_parking", "no_standing", "no_stopping", "no" ->
            return NoStreetParking
        "yes" ->
            return IncompleteStreetParking
        "separate" ->
            return StreetParkingSeparate
        else -> {
            val parkingOrientation = parkingValue.toParkingOrientation()
                // regard parking:lanes:*=marked as incomplete (because position is missing implicitly)
                ?: return if (parkingValue == "marked") IncompleteStreetParking else UnknownStreetParking

            val parkingPositionValue = tags["parking:lane$sideVal:$parkingValue"]
                // parking position is mandatory to be regarded as complete
                ?: return IncompleteStreetParking

            val parkingPosition = parkingPositionValue.toParkingPosition() ?: return UnknownStreetParking

            return StreetParkingPositionAndOrientation(parkingOrientation, parkingPosition)
        }
    }
}

private fun String.toParkingOrientation() = when (this) {
    "parallel" -> PARALLEL
    "diagonal" -> DIAGONAL
    "perpendicular" -> PERPENDICULAR
    else -> null
}

private fun String.toParkingPosition() = when (this) {
    "on_street", "on-street" -> ON_STREET
    "half_on_kerb" -> HALF_ON_KERB
    "on_kerb" -> ON_KERB
    "painted_area_only", "marked" -> PAINTED_AREA_ONLY
    "lay_by", "street_side", "bays" -> STREET_SIDE
    else -> null
}

private fun expandRelevantSidesTags(tags: Map<String, String>): Map<String, String> {
    val result = tags.toMutableMap()
    expandSidesTag("parking:lane", "", result)
    expandSidesTag("parking:condition", "", result)
    expandSidesTag("parking:lane", "parallel", result)
    expandSidesTag("parking:lane", "diagonal", result)
    expandSidesTag("parking:lane", "perpendicular", result)
    return result
}

/** Expand my_tag:both and my_tag into my_tag:left and my_tag:right etc */
private fun expandSidesTag(keyPrefix: String, keyPostfix: String, tags: MutableMap<String, String>) {
    val pre = keyPrefix
    val post = if (keyPostfix.isEmpty()) "" else ":$keyPostfix"
    val value = tags["$pre:both$post"] ?: tags["$pre$post"]
    if (value != null) {
        if (!tags.containsKey("$pre:left$post")) tags["$pre:left$post"] = value
        if (!tags.containsKey("$pre:right$post")) tags["$pre:right$post"] = value
    }
}
