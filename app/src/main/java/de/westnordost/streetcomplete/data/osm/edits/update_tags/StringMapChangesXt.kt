package de.westnordost.streetcomplete.data.osm.edits.update_tags

import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.ktx.copy
import java.util.*

fun Element.changesApplied(changes: StringMapChanges): Element {
    val copy = this.copy(newDateEdited = Date())
    try {
        if (copy.tags == null) throw ConflictException("The element has no tags")
        changes.applyTo(copy.tags)
    } catch (e: IllegalStateException) {
        throw ConflictException("Conflict while applying the changes")
    } catch (e: IllegalArgumentException) {
        /* There is a max key/value length limit of 255 characters in OSM. If we reach this
           point, it means the UI did permit an input of more than that. So, we have to catch
           this here latest.
           The UI should prevent this in the first place, at least
           for free-text input. For structured input, like opening hours, it is another matter
           because it's awkward to explain to a non-technical user this technical limitation

           See also https://github.com/openstreetmap/openstreetmap-website/issues/2025
          */
        throw ConflictException("Key or value is too long")
    }
    return copy
}
