package de.westnordost.streetcomplete.quests.opening_hours.model

import org.junit.Test

import org.junit.Assert.*

class OpeningMonthsTest {

    private val monday = weekdays(0b10000000)
    private val mondayToFriday = weekdays(0b11111000)
    private val saturdayToSunday = weekdays(0b00000110)

    private val wholeYear = CircularSection(0, 11)
    private val juneToSeptember = CircularSection(5, 8)

    @Test fun `omit months if whole year`() {
        assertEquals("Mo 09:00-17:00",
            months(wholeYear, cluster(days(monday, hours(9, 17)))).toString()
        )

        assertEquals("Mo-Fr 09:00-17:00; Sa,Su 09:00-12:00",
            months(wholeYear,
                cluster(days(mondayToFriday, hours(9, 17))),
                cluster(days(saturdayToSunday, hours(9, 12)))
            ).toString()
        )
    }

    @Test fun `prepend months before every cluster`() {
        assertEquals("Jun-Sep: Mo 09:00-17:00",
            months(juneToSeptember, cluster(days(monday, hours(9, 17)))).toString()
        )

        assertEquals("Jun-Sep: Mo-Fr 09:00-17:00; Jun-Sep: Sa,Su 09:00-12:00",
            months(juneToSeptember,
                cluster(days(mondayToFriday, hours(9, 17))),
                cluster(days(saturdayToSunday, hours(9, 12)))
            ).toString()
        )
    }

    @Test fun `prepend months before every weekdays`() {
        assertEquals("Jun-Sep: Mo-Fr 09:00-17:00, Jun-Sep: Sa,Su 09:00-12:00",
            months(juneToSeptember,
                cluster(
                    days(mondayToFriday, hours(9, 17)),
                    days(saturdayToSunday, hours(9, 12))
                )
            ).toString()
        )
    }

    private fun months(months: CircularSection, vararg clusters: List<OpeningWeekdays>) =
        OpeningMonths(months, clusters.toList())

    private fun cluster(vararg weekdays: OpeningWeekdays) = weekdays.toList()

    private fun weekdays(bits8: Int) = Weekdays(bits8.toBitField(8))

    private fun Int.toBitField(bits: Int) = (bits-1 downTo 0).map { this and (1 shl it) != 0 }.toBooleanArray()

    private fun days(weekdays: Weekdays, vararg ranges: TimeRange) =
        OpeningWeekdays(weekdays, ranges.toMutableList())

    private fun hours(start: Int, end: Int) = TimeRange(start * 60, end * 60)
}
