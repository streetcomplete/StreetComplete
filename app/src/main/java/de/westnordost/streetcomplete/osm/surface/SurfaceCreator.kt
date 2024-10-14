package de.westnordost.streetcomplete.osm.surface

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate

/** Apply the surface and note to the given [tags], with optional [prefix], e.g. "footway" for
 *  "footway:surface.
 *  By default the check date is also updated if the surface did not change, specified
 *  [updateCheckDate] = false if this should not be done. */
fun SurfaceAndNote.applyTo(tags: Tags, prefix: String? = null, updateCheckDate: Boolean = true) {
    val osmValue = surface?.osmValue
    requireNotNull(osmValue) { "Surface must be valid and not null" }

    val pre = if (prefix != null) "$prefix:" else ""
    val key = "${pre}surface"
    val previousOsmValue = tags[key]

    // remove smoothness, tracktype (etc), i.e. tags that are (potentially) associated with a given
    // surface type, as they can potentially be incorrect now that the surface changed (see #5951)
    if (previousOsmValue != osmValue) {
        getKeysAssociatedWithSurface(pre).forEach { tags.remove(it) }
    }

    // update surface + check date
    if (updateCheckDate) {
        tags.updateWithCheckDate(key, osmValue)
    } else {
        tags[key] = osmValue
    }

    // add/remove note - used to describe generic surfaces
    if (note != null) {
        tags["$key:note"] = note
    } else {
        tags.remove("$key:note")
    }

    // always clean up old source tags - source should be in changeset tags
    tags.remove("source:surface")
}
