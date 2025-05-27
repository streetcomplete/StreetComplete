package de.westnordost.streetcomplete.osm.surface

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.removeCheckDatesForKey
import de.westnordost.streetcomplete.osm.updateCheckDateForKey
import de.westnordost.streetcomplete.osm.updateWithCheckDate

/** Apply the surface to the given [tags], with optional [prefix], e.g. "footway" for
 *  "footway:surface.
 *  By default the check date is also updated if the surface did not change, specified
 *  [updateCheckDate] = false if this should not be done. */
fun Surface.applyTo(tags: Tags, prefix: String? = null, updateCheckDate: Boolean = true) {
    requireNotNull(osmValue) { "Surface must be valid and not null" }

    val pre = if (prefix != null) "$prefix:" else ""
    val key = "${pre}surface"
    val previousOsmValue = tags[key]

    // if previously it was an alias of the value we have now, we *don't* want to change the value!
    // (e.g. it was surface=earth before and now again Surface.DIRT was selected -> tag value
    // should not be changed)
    val previousWasAliasOfThis =
        previousOsmValue != null &&
        Surface.aliases.entries.find { it.key == previousOsmValue }?.value == this

    val hasChanged =
        previousOsmValue != null &&
        previousOsmValue != osmValue &&
        !previousWasAliasOfThis

    val hasChangedSurfaceCategory =
        hasChanged && parseSurfaceCategory(osmValue) != parseSurfaceCategory(previousOsmValue)

    val invalidTracktype =
        prefix == null && osmValue in INVALID_SURFACES_FOR_TRACKTYPES[tags["tracktype"]].orEmpty()

    // category of surface changed -> likely that tracktype is not correct anymore
    // if tracktype and surface don't match at all, also delete tracktype
    if (prefix == null && (hasChangedSurfaceCategory || invalidTracktype)) {
        tags.remove("tracktype")
        tags.removeCheckDatesForKey("tracktype")
    }

    if (hasChanged) {
        // need to remove keys associated with (old) surface
        getKeysAssociatedWithSurface(pre).forEach { tags.remove(it) }
    }

    val osmValueOrAlias = if (previousWasAliasOfThis) previousOsmValue else osmValue
    // update surface + check date
    if (updateCheckDate) {
        val isGeneric = this == Surface.PAVED || this == Surface.UNPAVED
        if (isGeneric) {
            // if a generic surface has been selected, always add the check date as a marker
            // that the selection has been deliberate and is not an artifact of prior coarse
            // satellite-imagery-based mapping ("oh, road is grey, must be *something* paved")
            tags.updateCheckDateForKey(key)
            tags[key] = osmValueOrAlias
        } else {
            tags.updateWithCheckDate(key, osmValueOrAlias)
        }
    } else {
        tags[key] = osmValueOrAlias
    }

    // always clean up old source tags - source should be in changeset tags
    tags.remove("source:surface")
}
