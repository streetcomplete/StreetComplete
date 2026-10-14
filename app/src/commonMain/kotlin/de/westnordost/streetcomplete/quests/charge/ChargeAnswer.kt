package de.westnordost.streetcomplete.quests.charge

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.duration.DurationUnit
import de.westnordost.streetcomplete.osm.removeCheckDatesForKey
import de.westnordost.streetcomplete.osm.updateCheckDateForKey
import de.westnordost.streetcomplete.osm.updateWithCheckDate

sealed interface ChargeAnswer {
    data object NoCharge : ChargeAnswer
}

/**
 * Represents a simple charge for a specific time unit.
 *
 * @property amount the cost value, e.g. 1.50
 * @property currencyCode the currency code of the charge, e.g. "EUR"
 * @property timeUnit the unit of time the amount applies to (e.g. per hour, per day)
 */
data class Charge(
    val amount: String,
    val currencyCode: String,
    val timeUnit: DurationUnit
) : ChargeAnswer

fun ChargeAnswer.applyTo(tags: Tags) {
    when (this) {
        is Charge -> {
            val unit = timeUnit.toOsmValue(false)
            // Format: "1.05 EUR/hour"
            tags["charge"] = "$amount $currencyCode/$unit"
            // just like with opening hours we *always* update the check date, because it is such
            // a volatile information
            tags.updateCheckDateForKey("charge")
        }
        ChargeAnswer.NoCharge -> {
            tags.updateWithCheckDate("fee", "no")
            tags.remove("fee:conditional")
            tags.remove("charge")
            tags.removeCheckDatesForKey("charge")
        }
    }
}
