package de.westnordost.streetcomplete.quests.shoulder

data class ShoulderSides(val left: Boolean, val right: Boolean)

val ShoulderSides.osmValue: String get() = when {
    left && right -> "both"
    left -> "left"
    right -> "right"
    else -> "no"
}
