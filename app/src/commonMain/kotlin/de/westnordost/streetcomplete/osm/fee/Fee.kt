package de.westnordost.streetcomplete.osm.fee

import de.westnordost.osm_opening_hours.model.OpeningHours
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate

sealed interface Fee {
    data object Yes : Fee
    data object No : Fee
    data class During(val hours: OpeningHours) : Fee
    data class ExceptDuring(val hours: OpeningHours) : Fee
}

fun Fee.applyTo(tags: Tags) {
    when (this) {
        is Fee.Yes -> {
            tags.updateWithCheckDate("fee", "yes")
            tags.remove("fee:conditional")
        }
        is Fee.No -> {
            tags.updateWithCheckDate("fee", "no")
            tags.remove("fee:conditional")
        }
        is Fee.During -> {
            tags.updateWithCheckDate("fee", "no")
            tags["fee:conditional"] = "yes @ ($hours)"
        }
        is Fee.ExceptDuring -> {
            tags.updateWithCheckDate("fee", "yes")
            tags["fee:conditional"] = "no @ ($hours)"
        }
    }
}
