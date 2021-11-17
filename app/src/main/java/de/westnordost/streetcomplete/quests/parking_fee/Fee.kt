package de.westnordost.streetcomplete.quests.parking_fee

import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.osm.opening_hours.parser.OpeningHoursRuleList


sealed class Fee

object HasFee : Fee()
object HasNoFee : Fee()
data class HasFeeAtHours(val openingHours: OpeningHoursRuleList) : Fee()
data class HasFeeExceptAtHours(val openingHours: OpeningHoursRuleList) : Fee()

fun Fee.applyTo(changes: StringMapChangesBuilder) {
    when(this) {
        is HasFee   -> {
            changes.updateWithCheckDate("fee", "yes")
            changes.deleteIfExists("fee:conditional")
        }
        is HasNoFee -> {
            changes.updateWithCheckDate("fee", "no")
            changes.deleteIfExists("fee:conditional")
        }
        is HasFeeAtHours -> {
            changes.updateWithCheckDate("fee", "no")
            changes.addOrModify("fee:conditional", "yes @ (${openingHours})")
        }
        is HasFeeExceptAtHours -> {
            changes.updateWithCheckDate("fee", "yes")
            changes.addOrModify("fee:conditional", "no @ (${openingHours})")
        }
    }
}
