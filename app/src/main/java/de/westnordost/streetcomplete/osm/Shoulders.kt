package de.westnordost.streetcomplete.osm

data class Shoulders(val left: Boolean, val right: Boolean)

/** Returns on which sides are shoulders. Returns null if there is no shoulders tagging at all */
fun createShoulders(tags: Map<String, String>, isLeftHandTraffic: Boolean): Shoulders? {
    val shoulder = createShouldersDefault(tags, isLeftHandTraffic)
    if (shoulder != null) return shoulder

    // alternative tagging
    val altShoulder = createShouldersAlternative(tags)
    if (altShoulder != null) return altShoulder

    // for motorways, a shoulder is (almost) implied, last chance to return non-null
    if (tags["highway"] == "motorway") {
        return createShouldersDefault(tags + mapOf("shoulder" to "yes"), isLeftHandTraffic)
    }

    return null
}

private fun createShouldersDefault(tags: Map<String, String>, isLeftHandTraffic: Boolean): Shoulders? = when (tags["shoulder"]) {
    "left" -> Shoulders(left = true, right = false)
    "right" -> Shoulders(left = false, right = true)
    "both" -> Shoulders(left = true, right = true)
    "yes" -> {
        val isForwardOneway = tags["oneway"] == "yes" || (tags["junction"] == "roundabout" && tags["oneway"] != "-1")
        val isReversedOneway = tags["oneway"] == "-1"
        val isOneway = isReversedOneway || isForwardOneway
        val isReverseSideRight = isReversedOneway xor isLeftHandTraffic
        if (isOneway) {
            Shoulders(left = isReverseSideRight, right = !isReverseSideRight)
        } else {
            Shoulders(left = true, right = true)
        }
    }
    "no" -> Shoulders(left = false, right = false)
    else -> null
}

private fun createShouldersAlternative(tags: Map<String, String>): Shoulders? {
    val shoulderLeft = tags["shoulder:both"] ?: tags["shoulder:left"]
    val shoulderRight = tags["shoulder:both"] ?: tags["shoulder:right"]
    return if (shoulderLeft != null || shoulderRight != null) {
        Shoulders(left = shoulderLeft == "yes", right = shoulderRight == "yes")
    } else {
        null
    }
}
