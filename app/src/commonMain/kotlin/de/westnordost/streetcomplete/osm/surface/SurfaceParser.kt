package de.westnordost.streetcomplete.osm.surface

fun parseSurface(surface: String?): Surface? {
    if (surface == null) return null

    if (surface in INVALID_SURFACES) return null

    val foundSurface = Surface.entries.find { it.osmValue == surface }
        ?: Surface.aliases.entries.find { it.key == surface }?.value

    if (foundSurface != null) return foundSurface

    // invalid characters in surface
    if (";" in surface || "<" in surface) return null

    return Surface.UNSUPPORTED
}
