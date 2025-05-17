package de.westnordost.streetcomplete.osm

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder

val KEYS_THAT_SHOULD_BE_REMOVED_WHEN_CHANGING_TO_STEPS = listOf(
    "smoothness"
).map { it.toRegex() }

fun StringMapChangesBuilder.changeToSteps() {
    for (key in keys) {
        if (KEYS_THAT_SHOULD_BE_REMOVED_WHEN_CHANGING_TO_STEPS.any { it.matches(key) }) {
            remove(key)
            removeCheckDatesForKey(key)
        }
    }

    this["highway"] = "steps"
}
