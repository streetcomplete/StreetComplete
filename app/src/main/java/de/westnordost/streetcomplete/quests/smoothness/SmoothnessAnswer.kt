package de.westnordost.streetcomplete.quests.smoothness

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.changeToSteps
import de.westnordost.streetcomplete.osm.removeCheckDatesForKey
import de.westnordost.streetcomplete.osm.updateWithCheckDate

sealed interface SmoothnessAnswer

data class SmoothnessValueAnswer(val value: Smoothness) : SmoothnessAnswer

data object IsActuallyStepsAnswer : SmoothnessAnswer
data object WrongSurfaceAnswer : SmoothnessAnswer

fun SmoothnessAnswer.applyTo(tags: Tags) {
    tags.remove("smoothness:date")
    // similar tag as smoothness, will be wrong/outdated when smoothness is set
    tags.remove("surface:grade")
    when (this) {
        is SmoothnessValueAnswer -> {
            tags.updateWithCheckDate("smoothness", value.osmValue)
        }
        is WrongSurfaceAnswer -> {
            tags.remove("surface")
            tags.remove("smoothness")
            tags.removeCheckDatesForKey("smoothness")
        }
        is IsActuallyStepsAnswer -> {
            tags.changeToSteps()
        }
    }
}
