package de.westnordost.streetcomplete.osm.surface

fun parseSurface(surface: String?): Surface? {
    if (surface == null) return null
    if (surface in INVALID_SURFACES) return null
    val foundSurface = Surface.entries.find { it.osmValue == surface }

    return foundSurface
        ?: if (";" in surface || "<" in surface) null else Surface.UNKNOWN
}
