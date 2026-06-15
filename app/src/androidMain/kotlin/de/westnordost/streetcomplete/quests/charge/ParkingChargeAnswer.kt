package de.westnordost.streetcomplete.quests.charge

import de.westnordost.streetcomplete.osm.duration.DurationUnit

sealed interface ParkingChargeAnswer

/**
 * Represents a simple charge for a specific time unit.
 *
 * @property amount the cost value, e.g. 1.50
 * @property currency the currency of the charge, e.g. "EUR"
 * @property timeUnit the unit of time the amount applies to (e.g. per hour, per day)
 */
data class SimpleCharge(
    val amount: Double,
    val currency: String,
    val timeUnit: DurationUnit
) : ParkingChargeAnswer
