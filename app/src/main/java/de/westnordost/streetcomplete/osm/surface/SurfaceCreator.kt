package de.westnordost.streetcomplete.osm.surface

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.removeCheckDatesForKey
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
    val hasChanged = previousOsmValue != null && previousOsmValue != osmValue

    val shouldRemoveTracktype = prefix == null && isSurfaceAndTracktypeConflicting(osmValue, tags["tracktype"])
    if (shouldRemoveTracktype) {
        tags.remove("tracktype")
        tags.removeCheckDatesForKey("tracktype")
    }

    // remove smoothness (etc) tags if surface was changed
    // or surface can be treated as outdated
    if (hasChanged || shouldRemoveTracktype) {
        getKeysAssociatedWithSurface(pre).forEach { tags.remove(it) }
    }

    // update surface + check date
    if (updateCheckDate) {
        tags.updateWithCheckDate(key, osmValue)
    } else {
        tags[key] = osmValue
    }

    // remove note if surface has changed
    if (hasChanged) {
        tags.remove("$key:note")
    }

    // always clean up old source tags - source should be in changeset tags
    tags.remove("source:surface")
}
