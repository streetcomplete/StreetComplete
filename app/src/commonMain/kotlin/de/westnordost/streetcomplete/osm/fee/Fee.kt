package de.westnordost.streetcomplete.osm.fee

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.opening_hours.toOpeningHours
import de.westnordost.streetcomplete.osm.time_restriction.TimeRestriction
import de.westnordost.streetcomplete.osm.updateWithCheckDate

sealed interface Fee {
    fun isComplete(): Boolean = when (this) {
        No -> true
        is Yes ->  timeRestriction?.isComplete() != false
    }

    data class Yes(val timeRestriction: TimeRestriction? = null) : Fee
    data object No : Fee
}

fun Fee.applyTo(tags: Tags) {
    when (this) {
        is Fee.Yes -> {
            when (timeRestriction?.mode) {
                TimeRestriction.Mode.ONLY_AT_HOURS -> {
                    tags.updateWithCheckDate("fee", "no")
                    tags["fee:conditional"] = "yes @ (${timeRestriction.hours.toOpeningHours()})"
                }
                TimeRestriction.Mode.EXCEPT_AT_HOURS -> {
                    tags.updateWithCheckDate("fee", "yes")
                    tags["fee:conditional"] = "no @ (${timeRestriction.hours.toOpeningHours()})"
                }
                null -> {
                    tags.updateWithCheckDate("fee", "yes")
                    tags.remove("fee:conditional")
                }
            }
        }
        is Fee.No -> {
            tags.updateWithCheckDate("fee", "no")
            tags.remove("fee:conditional")
        }
    }
}
