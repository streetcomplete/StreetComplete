package de.westnordost.streetcomplete.quests.parking_fee

import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.quests.opening_hours.model.OpeningHoursRuleList


sealed class FeeAnswer

object HasFee : FeeAnswer()
object HasNoFee : FeeAnswer()
data class HasFeeAtHours(val openingHours: OpeningHoursRuleList) : FeeAnswer()
data class HasFeeExceptAtHours(val openingHours: OpeningHoursRuleList) : FeeAnswer()

fun FeeAnswer.applyTo(changes: StringMapChangesBuilder) {
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
