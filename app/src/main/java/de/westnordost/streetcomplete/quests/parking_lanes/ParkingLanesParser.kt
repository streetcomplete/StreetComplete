package de.westnordost.streetcomplete.quests.parking_lanes

import de.westnordost.streetcomplete.quests.parking_lanes.ParkingLanePosition.*

data class LeftAndRightParkingLane(val left: ParkingLane?, val right: ParkingLane?)

fun createParkingLaneSides(tags: Map<String, String>): LeftAndRightParkingLane? {
    val expandedTags = expandRelevantSidesTags(tags)
    // then get the values for left and right
    val left = createParkingLaneForSide(expandedTags, "left")
    val right = createParkingLaneForSide(expandedTags, "right")

    if (left == null && right == null) return null

    return LeftAndRightParkingLane(left, right)
}

private fun createParkingLaneForSide(tags: Map<String, String>, side: String?): ParkingLane? {
    val sideVal = if (side != null) ":$side" else ""
    val parkingLaneValue = tags["parking:lane$sideVal"]
    val parkingLanePositionValue = tags["parking:lane$sideVal:$parkingLaneValue"]
    val parkingLanePosition = parkingLanePositionValue?.toParkingLanePosition()

    return when (parkingLaneValue) {
        "parallel" -> ParallelParkingLane(parkingLanePosition)
        "diagonal" -> DiagonalParkingLane(parkingLanePosition)
        "perpendicular" -> PerpendicularParkingLane(parkingLanePosition)
        "marked" -> MarkedParkingLane
        "no_parking" -> NoParking
        "no_stopping" -> NoStopping
        "fire_lane" -> FireLane
        "no" -> NoParkingLane
        null -> null
        else -> UnknownParkingLane
    }
}

private fun String.toParkingLanePosition() = when(this) {
    "on_street" -> ON_STREET
    "half_on_kerb" -> HALF_ON_KERB
    "on_kerb" -> ON_KERB
    "shoulder" -> SHOULDER
    "painted_area_only" -> PAINTED_AREA_ONLY
    "lay_by" -> LAY_BY
    else -> UNKNOWN
}

private fun expandRelevantSidesTags(tags: Map<String, String>): Map<String, String> {
    val result = tags.toMutableMap()
    expandSidesTag("parking:lane", "", result)
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