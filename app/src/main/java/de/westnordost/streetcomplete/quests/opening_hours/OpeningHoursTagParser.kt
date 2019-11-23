package de.westnordost.streetcomplete.quests.opening_hours

import ch.poole.openinghoursparser.OpeningHoursParser
import ch.poole.openinghoursparser.ParseException
import ch.poole.openinghoursparser.Rule
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningMonthsRow
import de.westnordost.streetcomplete.quests.opening_hours.adapter.OpeningWeekdaysRow
import de.westnordost.streetcomplete.quests.opening_hours.model.TimeRange
import de.westnordost.streetcomplete.quests.opening_hours.model.Weekdays
import java.io.ByteArrayInputStream

object OpeningHoursTagParser {
    // returns null for values that are invalid or not representable in
    // StreetComplete opening hours edit widget
    // otherwise returns data structure that can be directly used to
    // initialize this editing widget
    fun parse(openingHours: String): List<OpeningMonthsRow>? {

        var parsed = ""
        try {
            val input = ByteArrayInputStream(openingHours.toByteArray())
            val parser = OpeningHoursParser(input)
            val rules: ArrayList<Rule> = parser.rules(false)
            for (rule in rules) {
                parsed += rule.toDebugString()
            }
        } catch (e: ParseException) {
            e.printStackTrace()
            parsed = "parsing failed"
        }

        val dayData = BooleanArray(7)
        dayData[0] = true
        dayData[1] = false
        dayData[2] = true
        dayData[3] = false
        dayData[4] = true
        dayData[5] = false
        dayData[6] = true
        val data = listOf(OpeningMonthsRow())
        data[0].weekdaysList.add(OpeningWeekdaysRow(Weekdays(dayData), TimeRange(10, 20)))
        return data
    }

}
