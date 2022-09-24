package de.westnordost.streetcomplete.quests.incline_direction

import de.westnordost.streetcomplete.osm.Tags

enum class InclineDirection {
    UP, UP_REVERSED;

    fun applyTo(tags: Tags) {
        tags["incline"] = when (this) {
            UP -> "up"
            UP_REVERSED -> "down"
        }
    }
}
