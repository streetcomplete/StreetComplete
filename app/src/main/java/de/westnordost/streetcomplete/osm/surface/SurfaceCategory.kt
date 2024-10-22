package de.westnordost.streetcomplete.osm.surface

import de.westnordost.streetcomplete.osm.surface.SurfaceCategory.*

enum class SurfaceCategory {
    PAVED,
    UNPAVED,
    NATURAL,
    UNKNOWN
}

fun parseSurfaceCategory(surface: String?): SurfaceCategory? = when (surface) {
    in NATURAL_SURFACES -> NATURAL
    in UNPAVED_SURFACES -> UNPAVED
    in PAVED_SURFACES ->   PAVED
    null -> null
    else -> UNKNOWN
}
