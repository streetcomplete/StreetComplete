package de.westnordost.streetcomplete.osm.duration

import de.westnordost.streetcomplete.osm.duration.DurationUnit.*
import de.westnordost.streetcomplete.util.ktx.toShortString
import kotlinx.serialization.Serializable

/** A duration, as used in OSM for e.g. maxstay */
@Serializable
data class Duration(val value: Double, val unit: DurationUnit) {
    fun toOsmValue(): String =
        value.toShortString() + " " + when (unit) {
            MINUTES -> if (value != 1.0) "minutes" else "minute"
            HOURS -> if (value != 1.0) "hours" else "hour"
            DAYS -> if (value != 1.0) "days" else "day"
        }
}

enum class DurationUnit {
    MINUTES,
    HOURS,
    DAYS,
}
