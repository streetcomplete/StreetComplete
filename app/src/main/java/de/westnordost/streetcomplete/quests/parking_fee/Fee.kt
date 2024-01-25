package de.westnordost.streetcomplete.quests.parking_fee

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.opening_hours.parser.OpeningHoursRuleList
import de.westnordost.streetcomplete.osm.updateWithCheckDate

sealed interface Fee

data object HasFee : Fee
data object HasNoFee : Fee
data class HasFeeAtHours(val hours: OpeningHoursRuleList) : Fee
data class HasFeeExceptAtHours(val hours: OpeningHoursRuleList) : Fee

fun Fee.applyTo(tags: Tags) {
    when (this) {
        is HasFee -> {
            tags.updateWithCheckDate("fee", "yes")
            tags.remove("fee:conditional")
        }
        is HasNoFee -> {
            tags.updateWithCheckDate("fee", "no")
            tags.remove("fee:conditional")
        }
        is HasFeeAtHours -> {
            tags.updateWithCheckDate("fee", "no")
            tags["fee:conditional"] = "yes @ ($hours)"
        }
        is HasFeeExceptAtHours -> {
            tags.updateWithCheckDate("fee", "yes")
            tags["fee:conditional"] = "no @ ($hours)"
        }
    }
}
