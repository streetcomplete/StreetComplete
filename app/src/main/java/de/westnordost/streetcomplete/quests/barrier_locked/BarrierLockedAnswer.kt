package de.westnordost.streetcomplete.quests.barrier_locked

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.opening_hours.parser.OpeningHoursRuleList
import de.westnordost.streetcomplete.osm.updateWithCheckDate

sealed interface BarrierLockedAnswer

object Locked : BarrierLockedAnswer
object NotLocked : BarrierLockedAnswer
data class LockedAtHours(val hours: OpeningHoursRuleList) : BarrierLockedAnswer
data class LockedExceptAtHours(val hours: OpeningHoursRuleList) : BarrierLockedAnswer

fun BarrierLockedAnswer.applyTo(tags: Tags) {
    when (this) {
        is Locked -> {
            tags.updateWithCheckDate("locked", "yes")
            tags.remove("locked:conditional")
        }
        is NotLocked -> {
            tags.updateWithCheckDate("locked", "no")
            tags.remove("locked:conditional")
        }
        is LockedAtHours -> {
            tags.updateWithCheckDate("locked", "no")
            tags["locked:conditional"] = "yes @ ($hours)"
        }
        is LockedExceptAtHours -> {
            tags.updateWithCheckDate("locked", "yes")
            tags["locked:conditional"] = "no @ ($hours)"
        }
    }
}
