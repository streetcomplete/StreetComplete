package de.westnordost.streetcomplete.osm.surface

import de.westnordost.streetcomplete.osm.getLastCheckDateKeys

val INVALID_SURFACES = setOf(
    "cobblestone", // https://wiki.openstreetmap.org/wiki/Tag%3Asurface%3Dcobblestone
    "cement", // https://community.openstreetmap.org/t/mysterious-surface-cement/5158 and https://wiki.openstreetmap.org/wiki/Tag:surface%3Dconcrete
)

val SOFT_SURFACES = setOf("ground", "earth", "dirt", "grass", "sand", "mud", "ice", "salt", "snow", "woodchips")

val ANYTHING_UNPAVED = SOFT_SURFACES + setOf(
    "unpaved", "compacted", "gravel", "fine_gravel", "pebblestone", "grass_paver",
    // this ones are not strictly supported by aliased - so this is used by commonSurface* functions
    "earth", "mud",
)

val ANYTHING_FULLY_PAVED = setOf(
    "paved", "asphalt", "cobblestone", "cobblestone:flattened", "sett",
    "concrete", "concrete:plates", "paving_stones",
    "metal", "wood", "unhewn_cobblestone",
    // this ones are not strictly supported by aliased - so this is used by commonSurface* functions
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

class CyclewayFootwaySurfacesWithNote(val main: Surface?, val note: String?, val cycleway: Surface?, val cyclewayNote: String?, val footway: Surface?, val footwayNote: String?)

fun createSurfaceStatus(tags: Map<String, String>): CyclewayFootwaySurfacesWithNote {
    val surface = surfaceTextValueToSurfaceEnum(tags["surface"])
    val cyclewaySurface = surfaceTextValueToSurfaceEnum(tags["cycleway:surface"])
    val footwaySurface = surfaceTextValueToSurfaceEnum(tags["footway:surface"])
    val surfaceNote = tags["surface:note"]
    val cyclewaySurfaceNote = tags["cycleway:surface:note"]
    val footwaySurfaceNote = tags["footway:surface:note"]
    // we are treating surface=paved as not being specified at all
    // to show user an empty space to fill missing data
    // unless it has an associated note
    val surfaceIgnoringUnspecific = if (surface?.shouldBeDescribed == true && surfaceNote == null) { null } else { surface }
    var cyclewaySurfaceIgnoringUnspecific = if (cyclewaySurface?.shouldBeDescribed == true && cyclewaySurfaceNote == null) { null } else { cyclewaySurface }
    var footwaySurfaceIgnoringUnspecific = if (footwaySurface?.shouldBeDescribed == true && footwaySurfaceNote == null) { null } else { footwaySurface }
    if(tags["segregated"] == "yes") {
        if(cyclewaySurfaceIgnoringUnspecific == null) {
            cyclewaySurfaceIgnoringUnspecific = surfaceIgnoringUnspecific
        }
        if(footwaySurfaceIgnoringUnspecific == null) {
            footwaySurfaceIgnoringUnspecific = surfaceIgnoringUnspecific
        }
    }
    return CyclewayFootwaySurfacesWithNote(surfaceIgnoringUnspecific, surfaceNote, cyclewaySurfaceIgnoringUnspecific, cyclewaySurfaceNote, footwaySurfaceIgnoringUnspecific, footwaySurfaceNote)
}

data class SurfaceAndNoteMayBeEmpty(val value: Surface?, val note: String? = null)
/*
maybe just use SurfaceAndNote?
But then SurfaceAndNote.applyTo will need to throw exceptions on null value or rely on manual checks
ensuring otherwise that empty value will not be passed there
 */

/*
* to be used when only surface and surface:note tag is relevant
* for example if we want to tag road surface and we are free to skip sidewalk surface info
* */
fun createMainSurfaceStatus(tags: Map<String, String>): SurfaceAndNoteMayBeEmpty {
    val surface = surfaceTextValueToSurfaceEnum(tags["surface"])
    val surfaceNote = tags["surface:note"]
    val surfaceIgnoringUnspecific = if (surface?.shouldBeDescribed == true && surfaceNote == null) { null } else { surface }
    return SurfaceAndNoteMayBeEmpty(surfaceIgnoringUnspecific, surfaceNote)
}

fun surfaceTextValueToSurfaceEnum(surfaceValue: String?): Surface? {
    val foundSurface = Surface.values().find { it.osmValue == surfaceValue }

    // PAVED_AREA and UNPAVED_AREA are more generic - and this can be also asked
    // for objects which are not roads
    if (foundSurface == Surface.PAVED_ROAD) {
        return Surface.PAVED_AREA
    }
    if (foundSurface == Surface.UNPAVED_ROAD) {
        return Surface.UNPAVED_AREA
    }
    return foundSurface
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
    if (shared == "paved") {
        return Surface.PAVED_AREA
    }
    if (shared == "unpaved") {
        return Surface.UNPAVED_AREA
    }
    if (shared == "ground") {
        return Surface.GROUND_AREA
    }
    return Surface.values().firstOrNull { it.osmValue == shared }
}
