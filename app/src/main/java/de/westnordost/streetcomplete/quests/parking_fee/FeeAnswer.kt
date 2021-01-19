package de.westnordost.streetcomplete.quests.parking_fee

import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.quests.opening_hours.model.OpeningHoursRuleList


sealed class FeeAnswer {
    abstract fun applyTo(changes: StringMapChangesBuilder)
}

object HasFee : FeeAnswer() {
    override fun applyTo(changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate("fee", "yes")
        changes.deleteIfExists("fee:conditional")
    }
}

object HasNoFee : FeeAnswer() {
    override fun applyTo(changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate("fee", "no")
        changes.deleteIfExists("fee:conditional")
    }
}

data class HasFeeAtHours(val openingHours: OpeningHoursRuleList) : FeeAnswer() {
    override fun applyTo(changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate("fee", "no")
        changes.addOrModify("fee:conditional", "yes @ (${openingHours})")
    }
}

data class HasFeeExceptAtHours(val openingHours: OpeningHoursRuleList) : FeeAnswer() {
    override fun applyTo(changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate("fee", "yes")
        changes.addOrModify("fee:conditional", "no @ (${openingHours})")
    }
}
