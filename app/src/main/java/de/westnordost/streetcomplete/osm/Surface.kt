package de.westnordost.streetcomplete.osm

val ANYTHING_UNPAVED = setOf(
    "unpaved", "compacted", "gravel", "fine_gravel", "pebblestone", "grass_paver",
    "ground", "earth", "dirt", "grass", "sand", "mud", "ice", "salt", "snow", "woodchips"
)

val ANYTHING_PAVED = setOf(
    "paved", "asphalt", "cobblestone", "cobblestone:flattened", "sett",
    "concrete", "concrete:lanes", "concrete:plates", "paving_stones",
    "metal", "wood", "unhewn_cobblestone"
)
