package de.westnordost.streetcomplete.osm.maxstay

import de.westnordost.osm_opening_hours.model.OpeningHours
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.duration.Duration
import de.westnordost.streetcomplete.osm.updateWithCheckDate

sealed interface MaxStay {
    data object No : MaxStay
    data class Yes(val duration: Duration) : MaxStay
    data class During(val duration: Duration, val hours: OpeningHours) : MaxStay
    data class ExceptDuring(val duration: Duration, val hours: OpeningHours) : MaxStay
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
        is MaxStay.Yes -> {
            tags.updateWithCheckDate("maxstay", duration.toOsmValue())
            tags.remove("maxstay:conditional")
        }
        MaxStay.No -> {
            tags.updateWithCheckDate("maxstay", "no")
            tags.remove("maxstay:conditional")
        }
    }
}
