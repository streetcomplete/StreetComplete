package de.westnordost.streetcomplete.osm.street_parking

import de.westnordost.streetcomplete.osm.expandSidesTags
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.*
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.*

fun createStreetParkingSides(tags: Map<String, String>): LeftAndRightStreetParking? {
    // expand tags first so we do not need to deal with :both and naked tags
    val expandedTags = expandRelevantSidesTags(tags)
    // first try to parse new schema
    var left = createParkingForSide(expandedTags, "left")
    var right = createParkingForSide(expandedTags, "right")
    if (left == null && right == null) {
        // otherwise parse the old schema
        left = createParkingForSideOldSchema(expandedTags, "left")
        right = createParkingForSideOldSchema(expandedTags, "right")
    }

    if (left == null && right == null) return null

    return LeftAndRightStreetParking(left, right)
}

/** Parsing new schema:
 *  https://wiki.openstreetmap.org/wiki/Street_parking */
private fun createParkingForSide(tags: Map<String, String>, side: String): StreetParking? {
    val isStaggered = tags["parking:$side:staggered"] == "yes"
    val markings = tags["parking:$side:markings"]
    val hasMarkings = markings != "no" && markings != null

    val position = when (tags["parking:$side"]) {
        "lane" -> when {
            isStaggered && hasMarkings -> PAINTED_AREA_ONLY
            isStaggered -> STAGGERED_ON_STREET
            else -> ON_STREET
        }
        "half_on_kerb" -> if (isStaggered) STAGGERED_HALF_ON_STREET else HALF_ON_STREET
        "on_kerb" -> OFF_STREET
        // interpret "shoulder" as "off street", i.e. the same as "on_kerb". Reasons given in
        // https://wiki.openstreetmap.org/wiki/Talk:Street_parking#Suggestion_to_remove_parking:side=shoulder
        "shoulder" -> OFF_STREET
        "street_side" -> STREET_SIDE
        "no" -> return NoStreetParking
        "separate" -> return StreetParkingSeparate
        "yes" -> return IncompleteStreetParking
        null -> null
        else -> return UnknownStreetParking
    }

    val orientation = when (tags["parking:$side:orientation"]) {
        "parallel" -> PARALLEL
        "diagonal" -> DIAGONAL
        "perpendicular" -> PERPENDICULAR
        null -> null
        else -> return UnknownStreetParking
    }

    if (position == null && orientation == null) return null
    if (position == null || orientation == null) return IncompleteStreetParking

    return StreetParkingPositionAndOrientation(orientation, position)
}

/** Parsing old parking schema:
 *  https://wiki.openstreetmap.org/wiki/Key:parking:lane */
private fun createParkingForSideOldSchema(tags: Map<String, String>, side: String): StreetParking? {
    val parkingValue = tags["parking:lane:$side"] ?: return null

    when (parkingValue) {
        "no_parking", "no_standing", "no_stopping", "no" ->
            return NoStreetParking
        "yes" ->
            return IncompleteStreetParking
        "separate" ->
            return StreetParkingSeparate
        else -> {
            // regard parking:lanes:*=marked as incomplete (because position is missing implicitly)
            val parkingOrientation = when (parkingValue) {
                "parallel" -> PARALLEL
                "diagonal" -> DIAGONAL
                "perpendicular" -> PERPENDICULAR
                else -> null
            } ?: return if (parkingValue == "marked") IncompleteStreetParking else UnknownStreetParking

            val parkingPositionValue = tags["parking:lane:$side:$parkingValue"]
                // parking position is mandatory to be regarded as complete
                ?: return IncompleteStreetParking

            val parkingPosition = when (parkingPositionValue) {
                "on_street" -> ON_STREET
                "half_on_kerb" -> HALF_ON_STREET
                "on_kerb", "shoulder" -> OFF_STREET
                "painted_area_only" -> PAINTED_AREA_ONLY
                "lay_by", "street_side" -> STREET_SIDE

                else -> null
            } ?: return UnknownStreetParking

            return StreetParkingPositionAndOrientation(parkingOrientation, parkingPosition)
        }
    }
}

private fun expandRelevantSidesTags(tags: Map<String, String>): Map<String, String> {
    val result = tags.toMutableMap()
    result.expandSidesTags("parking:lane", "", true)
    result.expandSidesTags("parking:lane", "parallel", true)
    result.expandSidesTags("parking:lane", "diagonal", true)
    result.expandSidesTags("parking:lane", "perpendicular", true)
    result.expandSidesTags("parking", "", false)
    result.expandSidesTags("parking", "orientation", false)
    result.expandSidesTags("parking", "staggered", false)
    result.expandSidesTags("parking", "markings", false)
    return result
}
