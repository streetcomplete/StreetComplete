package de.westnordost.streetcomplete.osm.smoothness

val SMOOTHNESS_BAD_OR_WORSE_BUT_PASSABLE = setOf(
    "bad", "very_bad", "horrible", "very_horrible"
)

val SMOOTHNESS_BAD_OR_WORSE = SMOOTHNESS_BAD_OR_WORSE_BUT_PASSABLE + setOf(
    "impassable"
)
