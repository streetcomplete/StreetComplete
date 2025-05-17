package de.westnordost.streetcomplete.quests.parking_fee

import de.westnordost.osm_opening_hours.model.OpeningHours
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.quests.parking_fee.MaxStay.Unit.DAYS
import de.westnordost.streetcomplete.quests.parking_fee.MaxStay.Unit.HOURS
import de.westnordost.streetcomplete.quests.parking_fee.MaxStay.Unit.MINUTES
import de.westnordost.streetcomplete.util.ktx.toShortString

sealed interface MaxStay {
    enum class Unit { MINUTES, HOURS, DAYS }
}

data object NoMaxStay : MaxStay
data class MaxStayDuration(val value: Double, val unit: MaxStay.Unit) : MaxStay
data class MaxStayAtHours(val duration: MaxStayDuration, val hours: OpeningHours) : MaxStay
data class MaxStayExceptAtHours(val duration: MaxStayDuration, val hours: OpeningHours) : MaxStay

fun MaxStayDuration.toOsmValue(): String =
    value.toShortString() + " " + when (unit) {
        MINUTES -> if (value != 1.0) "minutes" else "minute"
        HOURS -> if (value != 1.0) "hours" else "hour"
        DAYS -> if (value != 1.0) "days" else "day"
    }

fun MaxStay.applyTo(tags: Tags) {
    when (this) {
        is MaxStayExceptAtHours -> {
            tags.updateWithCheckDate("maxstay", duration.toOsmValue())
            tags["maxstay:conditional"] = "no @ ($hours)"
        }
        is MaxStayAtHours -> {
            tags.updateWithCheckDate("maxstay", "no")
            tags["maxstay:conditional"] = "${duration.toOsmValue()} @ ($hours)"
        }
        is MaxStayDuration -> {
            tags.updateWithCheckDate("maxstay", toOsmValue())
            tags.remove("maxstay:conditional")
        }
        NoMaxStay -> {
            tags.updateWithCheckDate("maxstay", "no")
            tags.remove("maxstay:conditional")
        }
    }
}
