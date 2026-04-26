package de.westnordost.streetcomplete.osm.time_restriction

import de.westnordost.streetcomplete.osm.opening_hours.HierarchicOpeningHours
import kotlinx.serialization.Serializable

/** A (time-based) restriction of something.  */
@Serializable
data class TimeRestriction(
    val hours: HierarchicOpeningHours,
    val mode: Mode
) {
    fun isComplete(): Boolean = hours.isComplete()

    enum class Mode { ONLY_AT_HOURS, EXCEPT_AT_HOURS }
}
