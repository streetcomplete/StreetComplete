package de.westnordost.streetcomplete.osm.duration

import de.westnordost.streetcomplete.osm.duration.DurationUnit.*
import de.westnordost.streetcomplete.util.ktx.toShortString
import kotlinx.serialization.Serializable

/** A duration, as used in OSM for e.g. maxstay or charge */
@Serializable
data class Duration(val value: Double, val unit: DurationUnit) {
    fun toOsmValue(alwaysSingular: Boolean = false, ignoreValue: Boolean = false): String {
        var result = ""
        if (!ignoreValue) {
            result += value.toShortString() + " "
        }
        result += when (unit) {
            MINUTES -> if (value != 1.0 && !alwaysSingular) "minutes" else "minute"
            HOURS -> if (value != 1.0 && !alwaysSingular) "hours" else "hour"
            DAYS -> if (value != 1.0 && !alwaysSingular) "days" else "day"
        }
        return result
    }
}

enum class DurationUnit {
    MINUTES,
    HOURS,
    DAYS,
}
