package de.westnordost.streetcomplete.quests.incline_direction

import de.westnordost.streetcomplete.osm.Tags

sealed interface InclineDirectionAnswer
class RegularInclineDirectionAnswer(val value: RegularInclineDirection) : InclineDirectionAnswer
object UpdAndDownHopsAnswer : InclineDirectionAnswer

enum class RegularInclineDirection {
    UP, UP_REVERSED
}

fun InclineDirectionAnswer.applyTo(tags: Tags) {
    tags["incline"] = when (this) {
        is RegularInclineDirectionAnswer -> when (this.value) {
            RegularInclineDirection.UP -> "up"
            RegularInclineDirection.UP_REVERSED -> "down"
        }
        is UpdAndDownHopsAnswer -> "up/down"
    }
}
