package de.westnordost.streetcomplete.osm


private val KEYS_THAT_SHOULD_BE_REMOVED_WHEN_CHANGING_TO_STEPS = listOf(
    "smoothness"
).map { it.toRegex() }

fun Tags.changeToSteps() {
    for (key in keys) {
        if (KEYS_THAT_SHOULD_BE_REMOVED_WHEN_CHANGING_TO_STEPS.any { it.matches(key) }) {
            remove(key)
            removeCheckDatesForKey(key)
        }
    }

    this["highway"] = "steps"
}
