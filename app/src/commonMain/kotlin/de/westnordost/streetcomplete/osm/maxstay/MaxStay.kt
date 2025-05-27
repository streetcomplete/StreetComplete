package de.westnordost.streetcomplete.osm.maxstay

import de.westnordost.osm_opening_hours.model.OpeningHours
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.osm.maxstay.MaxStay.Unit.DAYS
import de.westnordost.streetcomplete.osm.maxstay.MaxStay.Unit.HOURS
import de.westnordost.streetcomplete.osm.maxstay.MaxStay.Unit.MINUTES
import de.westnordost.streetcomplete.util.ktx.toShortString

sealed interface MaxStay {
    enum class Unit { MINUTES, HOURS, DAYS }

    data object No : MaxStay
    data class Duration(val value: Double, val unit: Unit) : MaxStay
    data class During(val duration: Duration, val hours: OpeningHours) : MaxStay
    data class ExceptDuring(val duration: Duration, val hours: OpeningHours) : MaxStay
}

fun MaxStay.Duration.toOsmValue(): String =
    value.toShortString() + " " + when (unit) {
        MINUTES -> if (value != 1.0) "minutes" else "minute"
        HOURS -> if (value != 1.0) "hours" else "hour"
        DAYS -> if (value != 1.0) "days" else "day"
    }

fun MaxStay.applyTo(tags: Tags) {
    when (this) {
        is MaxStay.ExceptDuring -> {
            tags.updateWithCheckDate("maxstay", duration.toOsmValue())
            tags["maxstay:conditional"] = "no @ ($hours)"
        }
        is MaxStay.During -> {
            tags.updateWithCheckDate("maxstay", "no")
            tags["maxstay:conditional"] = "${duration.toOsmValue()} @ ($hours)"
        }
        is MaxStay.Duration -> {
            tags.updateWithCheckDate("maxstay", toOsmValue())
            tags.remove("maxstay:conditional")
        }
        MaxStay.No -> {
            tags.updateWithCheckDate("maxstay", "no")
            tags.remove("maxstay:conditional")
        }
    }
}
