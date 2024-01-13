package de.westnordost.streetcomplete.data.elementfilter.filters

import de.westnordost.streetcomplete.util.ktx.minusInSystemTimeZone
import de.westnordost.streetcomplete.util.ktx.now
import de.westnordost.streetcomplete.util.ktx.plusInSystemTimeZone
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.math.absoluteValue

interface DateFilter {
    val date: LocalDate
}

/** A date relative to (start of) today (positive: future, negative: past) */
class RelativeDate(val deltaDays: Float) : DateFilter {
    override val date: LocalDate get() {
        val now = LocalDateTime.now()
        val plusHours = (deltaDays * MULTIPLIER * 24).toLong()
        val relativeDateTime = (
            if (plusHours > 0) {
                now.plusInSystemTimeZone(plusHours, DateTimeUnit.HOUR)
            } else {
                now.minusInSystemTimeZone(plusHours.absoluteValue, DateTimeUnit.HOUR)
            }
        )
        return relativeDateTime.date
    }

    override fun toString() = "$deltaDays days"

    companion object {
        var MULTIPLIER: Float = 1f
    }
}

/** A fixed date */
class FixedDate(override val date: LocalDate) : DateFilter {
    override fun toString() = date.toString()
}
