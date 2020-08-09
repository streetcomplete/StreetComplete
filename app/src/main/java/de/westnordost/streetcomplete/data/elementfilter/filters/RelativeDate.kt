package de.westnordost.streetcomplete.data.elementfilter.filters

import java.util.*

interface DateFilter {
    val date: Date
}

/** A date relative to (start of) today (positive: future, negative: past) */
class RelativeDate(val deltaDays: Float): DateFilter {
    override val date: Date get() {
        val cal: Calendar = Calendar.getInstance()
        cal.add(Calendar.SECOND, (deltaDays * 24 * 60 * 60).toInt())
        return cal.time
    }
}

class FixedDate(override val date: Date): DateFilter