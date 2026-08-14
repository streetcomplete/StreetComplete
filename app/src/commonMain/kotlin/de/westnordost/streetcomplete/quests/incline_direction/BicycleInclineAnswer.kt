package de.westnordost.streetcomplete.quests.incline_direction

import de.westnordost.streetcomplete.osm.Tags

sealed interface BicycleInclineAnswer
class RegularBicycleInclineAnswer(val value: Incline) : BicycleInclineAnswer
data object UpAndDownHopsAnswer : BicycleInclineAnswer

fun BicycleInclineAnswer.applyTo(tags: Tags) {
    when (this) {
        is RegularBicycleInclineAnswer -> value.applyTo(tags)
        is UpAndDownHopsAnswer -> tags["incline"] = "up/down"
    }
}
