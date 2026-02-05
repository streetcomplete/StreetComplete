package de.westnordost.streetcomplete.osm.maxstay

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.duration.Duration
import de.westnordost.streetcomplete.osm.opening_hours.toOpeningHours
import de.westnordost.streetcomplete.osm.time_restriction.TimeRestriction
import de.westnordost.streetcomplete.osm.updateWithCheckDate

/** Maximum time a vehicle may stay at a parking. Optionally, the max stay duration may be only or
 *  exclusively be valid within a certain time range */
data class MaxStay(
    val duration: Duration?,
    val timeRestriction: TimeRestriction? = null
) {
    fun isComplete(): Boolean =
        duration != null && timeRestriction?.isComplete() != false
}

fun MaxStay.applyTo(tags: Tags) {
    require(duration != null)
    val durationStr = duration.toOsmValue()
    when (timeRestriction?.mode) {
        TimeRestriction.Mode.ONLY_AT_HOURS -> {
            tags.updateWithCheckDate("maxstay", "no")
            tags["maxstay:conditional"] = "$durationStr @ (${timeRestriction.hours.toOpeningHours()})"
        }
        TimeRestriction.Mode.EXCEPT_AT_HOURS -> {
            tags.updateWithCheckDate("maxstay", durationStr)
            tags["maxstay:conditional"] = "no @ (${timeRestriction.hours.toOpeningHours()})"
        }
        null -> {
            tags.updateWithCheckDate("maxstay", durationStr)
            tags.remove("maxstay:conditional")
        }
    }
}
