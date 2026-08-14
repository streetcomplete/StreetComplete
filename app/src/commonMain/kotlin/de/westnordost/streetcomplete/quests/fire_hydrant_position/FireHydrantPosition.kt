package de.westnordost.streetcomplete.quests.fire_hydrant_position

import kotlinx.serialization.Serializable

@Serializable
enum class FireHydrantPosition(val osmValue: String) {
    GREEN("green"),
    LANE("lane"),
    SIDEWALK("sidewalk"),
    PARKING_LOT("parking_lot"),
}
