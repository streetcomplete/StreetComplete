package de.westnordost.streetcomplete.quests.barrier_locked

import de.westnordost.osm_opening_hours.model.OpeningHours
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate

sealed interface BarrierLockedAnswer

object Locked : BarrierLockedAnswer
object NotLocked : BarrierLockedAnswer
data class LockedAtHours(val hours: OpeningHours) : BarrierLockedAnswer
data class LockedExceptAtHours(val hours: OpeningHours) : BarrierLockedAnswer

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
