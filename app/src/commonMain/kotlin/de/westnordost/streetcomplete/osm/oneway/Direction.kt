package de.westnordost.streetcomplete.osm.oneway

import kotlinx.serialization.Serializable

@Serializable
enum class Direction {
    FORWARD,
    BACKWARD,
    BOTH;

    fun reverse(): Direction = when (this) {
        FORWARD -> BACKWARD
        BACKWARD -> FORWARD
        BOTH -> BOTH
    }

    companion object {
        /** Return the default direction of a oneway if nothing is specified */
        fun getDefault(isRightSide: Boolean, isLeftHandTraffic: Boolean): Direction =
            if (isRightSide xor isLeftHandTraffic) FORWARD else BACKWARD
    }
}
