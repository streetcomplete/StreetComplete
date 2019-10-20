package de.westnordost.streetcomplete.quests.postbox_collection_times

import de.westnordost.streetcomplete.quests.opening_hours.model.Weekdays
import java.util.*

data class WeekdaysTimes(var weekdays: Weekdays, var minutesList: MutableList<Int>) {

    override fun toString() =
        weekdays.toString() + " " + minutesList.joinToString(",") {
            "%02d:%02d".format(Locale.US, it / 60, it % 60)
        }
}
