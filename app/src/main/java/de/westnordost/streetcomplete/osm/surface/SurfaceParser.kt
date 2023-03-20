package de.westnordost.streetcomplete.osm.surface

/** Parse the surface and optional associated note from the given [tags].
 *  Specify a [prefix] if you want for example the surface of the "footway" (then looks for
 *  "footway:surface") */
fun createSurfaceAndNote(tags: Map<String, String>, prefix: String? = null): SurfaceAndNote? {
    val pre = if (prefix != null) "$prefix:" else ""
    val note = tags["${pre}surface:note"]
    val surface = createSurface(tags["${pre}surface"])
    if (surface == null && note == null) return null
    return SurfaceAndNote(surface, note)
}

fun createSurface(surface: String?): Surface? {
    if (surface == null) return null
    if (surface in INVALID_SURFACES) return null
    val foundSurface = Surface.values().find { it.osmValue == surface }

    return foundSurface
        ?: if (";" in surface || "<" in surface) null else Surface.UNKNOWN
}
