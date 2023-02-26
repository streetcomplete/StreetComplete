package de.westnordost.streetcomplete.osm.surface

import de.westnordost.streetcomplete.osm.getLastCheckDateKeys

val INVALID_SURFACES = setOf(
    "cobblestone", // https://wiki.openstreetmap.org/wiki/Tag%3Asurface%3Dcobblestone
    "cement", // https://community.openstreetmap.org/t/mysterious-surface-cement/5158 and https://wiki.openstreetmap.org/wiki/Tag:surface%3Dconcrete
)

val SOFT_SURFACES = setOf("ground", "earth", "dirt", "grass", "sand", "mud", "ice", "salt", "snow", "woodchips")

val ANYTHING_UNPAVED = SOFT_SURFACES + setOf(
    "unpaved", "compacted", "gravel", "fine_gravel", "pebblestone", "grass_paver"
)

val ANYTHING_FULLY_PAVED = setOf(
    "paved", "asphalt", "cobblestone", "cobblestone:flattened", "sett",
    "concrete", "concrete:plates", "paving_stones",
    "metal", "wood", "unhewn_cobblestone", "chipseal",
    "brick", "bricks", "cobblestone:flattened", "paving_stones:30",
)

val ANYTHING_PAVED = ANYTHING_FULLY_PAVED + setOf(
    "concrete:lanes"
)

val INVALID_SURFACES_FOR_TRACKTYPES = mapOf(
    "grade1" to ANYTHING_UNPAVED,
    "grade2" to SOFT_SURFACES,
    "grade3" to ANYTHING_FULLY_PAVED,
    "grade4" to ANYTHING_FULLY_PAVED,
    "grade5" to ANYTHING_FULLY_PAVED,
)

fun isSurfaceAndTracktypeMismatching(surface: String, tracktype: String): Boolean =
    INVALID_SURFACES_FOR_TRACKTYPES[tracktype]?.contains(surface) == true

fun keysToBeRemovedOnSurfaceChange(prefix: String): Set<String> =
    setOf(
        "${prefix}surface:grade",
        "${prefix}smoothness",
        "${prefix}smoothness:date",
        "${prefix}smoothness",
        "${prefix}surface:colour",
        "source:${prefix}surface") +
    getLastCheckDateKeys("${prefix}surface") +
    getLastCheckDateKeys("${prefix}smoothness")

class ParsedCyclewayFootwaySurfacesWithNote(val main: ParsedSurfaceAndNote, val cycleway: ParsedSurfaceAndNote, val footway: ParsedSurfaceAndNote)

fun createSurfaceStatus(tags: Map<String, String>): ParsedCyclewayFootwaySurfacesWithNote {
    val surfaceNote = tags["surface:note"]
    val surface = parseSingleSurfaceTag(tags["surface"], surfaceNote)
    val cyclewaySurfaceNote = tags["cycleway:surface:note"]
    val cyclewaySurface = parseSingleSurfaceTag(tags["cycleway:surface"], cyclewaySurfaceNote)
    val footwaySurfaceNote = tags["footway:surface:note"]
    val footwaySurface = parseSingleSurfaceTag(tags["footway:surface"], footwaySurfaceNote)
    return ParsedCyclewayFootwaySurfacesWithNote(
        ParsedSurfaceAndNote(surface, surfaceNote),
        ParsedSurfaceAndNote(cyclewaySurface, cyclewaySurfaceNote),
        ParsedSurfaceAndNote(footwaySurface, footwaySurfaceNote))
}

data class ParsedSurfaceAndNote(val value: Surface?, val note: String? = null)

/*
* to be used when only surface and surface:note tag is relevant
* for example if we want to tag road surface and we are free to skip sidewalk surface info
* */
fun createMainSurfaceStatus(tags: Map<String, String>): ParsedSurfaceAndNote {
    val surfaceNote = tags["surface:note"]
    return ParsedSurfaceAndNote(parseSingleSurfaceTag(tags["surface"], surfaceNote), surfaceNote)
}

fun parseSingleSurfaceTag(surfaceTag: String?, surfaceNote: String?): Surface? {
    if (surfaceTag == null) {
        return null
    }
    if (surfaceTag in INVALID_SURFACES) {
        return null
    }
    // we are treating surface=paved as not being specified at all
    // to show user an empty space to fill missing data
    // unless it has an associated note
    val surface = surfaceTextValueToSurfaceEnum(surfaceTag)
    val surfaceIgnoringUnspecific = if (surface?.shouldBeDescribed == true && surfaceNote == null) null else surface
    if (surface == null) {
        if (";" in surfaceTag || "<" in surfaceTag) {
            // invalid surface tag, result of a botched merge, can and should be treated as requiring replacement
            return null
        }
        return Surface.UNKNOWN_SURFACE
    }
    return surfaceIgnoringUnspecific
}

fun surfaceTextValueToSurfaceEnum(surfaceValue: String?): Surface? {
    val foundSurface = Surface.values().find { it.osmValue == surfaceValue }

    // PAVED_AREA and UNPAVED_AREA are more generic - and this can be also asked
    // for objects which are not roads
    return when (foundSurface) {
        Surface.PAVED_ROAD -> Surface.PAVED_AREA
        Surface.UNPAVED_ROAD -> Surface.UNPAVED_AREA
        else -> foundSurface
    }
}

fun commonSurfaceDescription(surfaceA: String?, surfaceB: String?): String? {
    if (surfaceA == null || surfaceB == null) {
        return null
    }
    if (surfaceA == surfaceB) {
        return surfaceA
    }
    if (surfaceA in ANYTHING_PAVED && surfaceB in ANYTHING_PAVED) {
        return "paved"
    }
    if (surfaceA in ANYTHING_UNPAVED && surfaceB in ANYTHING_UNPAVED) {
        return "unpaved"
    }
    return null
}

fun commonSurfaceObject(surfaceA: String?, surfaceB: String?): Surface? {
    val shared = commonSurfaceDescription(surfaceA, surfaceB) ?: return null
    return when (shared) {
        "paved" -> Surface.PAVED_AREA
        "unpaved" -> Surface.UNPAVED_AREA
        "ground" -> Surface.GROUND_AREA
        else -> Surface.values().firstOrNull { it.osmValue == shared }
    }
}
