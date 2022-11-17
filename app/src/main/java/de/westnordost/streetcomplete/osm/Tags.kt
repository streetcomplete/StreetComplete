package de.westnordost.streetcomplete.osm

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder

typealias Tags = StringMapChangesBuilder

fun Tags.expandSides(key: String, includeBareTag: Boolean) {
    val both = get("$key:both") ?: (if (includeBareTag) get(key) else null)
    if (both != null) {
        // *:left/right is seen as more specific/correct in case the two contradict each other
        if (!containsKey("$key:left")) set("$key:left", both)
        if (!containsKey("$key:right")) set("$key:right", both)
    }
    remove("$key:both")
    if (includeBareTag) remove(key)
}

fun Tags.mergeSides(key: String) {
    val left = get("$key:left")
    val right = get("$key:right")
    if (left != null && left == right) {
        set("$key:both", left)
        remove("$key:left")
        remove("$key:right")
    }
}
