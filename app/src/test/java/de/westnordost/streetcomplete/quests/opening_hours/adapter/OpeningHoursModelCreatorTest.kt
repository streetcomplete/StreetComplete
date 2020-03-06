package de.westnordost.streetcomplete.quests.opening_hours.adapter

import org.junit.Test

import de.westnordost.streetcomplete.quests.opening_hours.model.CircularSection
import de.westnordost.streetcomplete.quests.opening_hours.model.OpeningMonths
import de.westnordost.streetcomplete.quests.opening_hours.model.OpeningWeekdays
import de.westnordost.streetcomplete.quests.opening_hours.model.TimeRange
import de.westnordost.streetcomplete.quests.opening_hours.model.Weekdays

import org.junit.Assert.*

class OpeningHoursModelCreatorTest {

    private val monday = Weekdays(booleanArrayOf(true))
    private val mondayToTuesday = Weekdays(booleanArrayOf(true, true))
    private val mondayToFriday = Weekdays(booleanArrayOf(true, true, true, true, true))
    private val tuesdayToFriday = Weekdays(booleanArrayOf(false, true, true, true, true))
    private val tuesday = Weekdays(booleanArrayOf(false, true))
    private val weekend = Weekdays(booleanArrayOf(false, false, false, false, false, true, true))

    private val morning = TimeRange(8 * 60, 12 * 60, false)
    private val lateMorning = TimeRange(9 * 60, 12 * 60, false)
    private val workingDay = TimeRange(9 * 60, 17 * 60, false)
    private val midday = TimeRange(10 * 60, 16 * 60, false)
    private val afternoon = TimeRange(14 * 60, 18 * 60, false)
    private val longAfternoon = TimeRange(13 * 60, 20 * 60, false)
    private val duskTillDawn = TimeRange(18 * 60, 6 * 60, false)
    private val earlyMorning = TimeRange(4 * 60, 8 * 60, false)

    private val mondayMorning = OpeningWeekdaysRow(monday, morning)
    private val mondayAfternoon = OpeningWeekdaysRow(monday, afternoon)
    private val mondayWorkingDay = OpeningWeekdaysRow(monday, workingDay)
    private val tuesdayMorning = OpeningWeekdaysRow(tuesday, morning)
    private val tuesdayLateMorning = OpeningWeekdaysRow(tuesday, lateMorning)
    private val mondayTuesdayMorning = OpeningWeekdaysRow(mondayToTuesday, morning)
    private val tuesdayToFridayAfternoon = OpeningWeekdaysRow(tuesdayToFriday, afternoon)
    private val workweekMorning = OpeningWeekdaysRow(mondayToFriday, morning)
    private val workweek = OpeningWeekdaysRow(mondayToFriday, workingDay)
    private val weekendLateMorning = OpeningWeekdaysRow(weekend, lateMorning)


    private val wholeYear = CircularSection(0, 11)
    private val juneToSeptember = CircularSection(5, 8)
    private val julyToDecember = CircularSection(6, 11)

    @Test fun `merges opening weekdays rows of same day`() {
        val actual = create(
            OpeningWeekdaysRow(monday, morning),
            OpeningWeekdaysRow(monday, afternoon)
        )

        val expected = months(OpeningMonths(wholeYear, clusters(
            weekdays(OpeningWeekdays(monday, times(morning, afternoon)))
        )))

        assertEquals(expected, actual)
    }

    @Test fun `does not cluster different weekdays`() {
        val actual = create(
            OpeningWeekdaysRow(monday, morning),
            OpeningWeekdaysRow(tuesday, afternoon)
        )

        val expected = months(OpeningMonths(wholeYear, clusters(
            weekdays(OpeningWeekdays(monday, times(morning))),
            weekdays(OpeningWeekdays(tuesday, times(afternoon)))
        )))

        assertEquals(expected, actual)
    }

    @Test fun `clusters overlapping weekdays`() {
        val actual = create(
            OpeningWeekdaysRow(monday, morning),
            OpeningWeekdaysRow(mondayToTuesday, afternoon)
        )

        val expected = months(OpeningMonths(wholeYear, clusters(weekdays(
            OpeningWeekdays(monday, times(morning)),
            OpeningWeekdays(mondayToTuesday, times(afternoon))
        ))))

        assertEquals(expected, actual)
    }

    @Test fun `does not cluster intersecting times`() {
        val actual = create(
            OpeningWeekdaysRow(monday, morning),
            OpeningWeekdaysRow(mondayToTuesday, midday)
        )

        val expected = months(OpeningMonths(wholeYear, clusters(
            weekdays(OpeningWeekdays(monday, times(morning))),
            weekdays(OpeningWeekdays(mondayToTuesday, times(midday)))
        )))

        assertEquals(expected, actual)
    }

    @Test fun `does not cluster intersecting times in cluster`() {
        val actual = create(
            OpeningWeekdaysRow(monday, morning),
            OpeningWeekdaysRow(mondayToTuesday, afternoon),
            OpeningWeekdaysRow(tuesday, midday)
        )

        val expected = months(OpeningMonths(wholeYear, clusters(
            weekdays(
                OpeningWeekdays(monday, times(morning)),
                OpeningWeekdays(mondayToTuesday, times(afternoon))
            ),
            weekdays(
                OpeningWeekdays(tuesday, times(midday))
            )
        )))

        assertEquals(expected, actual)
    }

    @Test fun `does cluster multiple overlapping weekdays with non-intersecting times`() {
        val actual = create(
            OpeningWeekdaysRow(mondayToFriday, morning),
            OpeningWeekdaysRow(monday, afternoon),
            OpeningWeekdaysRow(tuesday, longAfternoon)
        )

        val expected = months(OpeningMonths(wholeYear, clusters(weekdays(
            OpeningWeekdays(mondayToFriday, times(morning)),
            OpeningWeekdays(monday, times(afternoon)),
            OpeningWeekdays(tuesday, times(longAfternoon))
        ))))

        assertEquals(expected, actual)
    }

    @Test fun `does cluster weekdays that overlap because time range extends to next day`() {
        val actual = create(
            OpeningWeekdaysRow(monday, duskTillDawn),
            OpeningWeekdaysRow(tuesday, afternoon)
        )

        val expected = months(OpeningMonths(wholeYear, clusters(weekdays(
            OpeningWeekdays(monday, times(duskTillDawn)),
            OpeningWeekdays(tuesday, times(afternoon))
        ))))

        assertEquals(expected, actual)
    }

    @Test fun `does not cluster weekdays that overlap because time range extends to next day with overlapping times`() {
    // nnnnewww function name record!!! 🎉
        val actual = create(
            OpeningWeekdaysRow(monday, duskTillDawn),
            OpeningWeekdaysRow(tuesday, earlyMorning)
        )

        val expected = months(OpeningMonths(wholeYear, clusters(
            weekdays(OpeningWeekdays(monday, times(duskTillDawn))),
            weekdays(OpeningWeekdays(tuesday, times(earlyMorning)))
        )))

        assertEquals(expected, actual)
    }

    private fun create(vararg rows: OpeningWeekdaysRow): List<OpeningMonths> {
        val omr = OpeningMonthsRow()
        omr.weekdaysList.addAll(rows)
        return listOf(omr).toOpeningMonthsList()
    }

    @Test fun `test tag generation`() {
        assertEquals("Mo 08:00-12:00", OpeningMonthsRow(wholeYear, mondayMorning).toString())
        assertEquals("Mo 08:00-12:00,14:00-18:00", OpeningMonthsRow(wholeYear, mutableListOf(mondayMorning, mondayAfternoon)).toString())
        assertEquals("Mo 08:00-12:00; Tu 08:00-12:00", OpeningMonthsRow(wholeYear, mutableListOf(mondayMorning, tuesdayMorning)).toString())
        assertEquals("Mo,Tu 08:00-12:00, Tu-Fr 14:00-18:00", OpeningMonthsRow(wholeYear, mutableListOf(mondayTuesdayMorning, tuesdayToFridayAfternoon)).toString())
        assertEquals("Mo,Tu 08:00-12:00; Tu 09:00-12:00", OpeningMonthsRow(wholeYear, mutableListOf(mondayTuesdayMorning, tuesdayLateMorning)).toString())
        assertEquals("Mo-Fr 08:00-12:00; Tu 09:00-12:00", OpeningMonthsRow(wholeYear, mutableListOf(workweekMorning, tuesdayLateMorning)).toString())
    }

    @Test fun `omit months if whole year in generated tag`() {
        assertEquals("Tu 08:00-12:00", OpeningMonthsRow(wholeYear, tuesdayMorning).toString())
    }

    @Test fun `prepend months before every cluster`() {
        assertEquals("Jun-Sep: Mo 09:00-17:00", OpeningMonthsRow(juneToSeptember, mondayWorkingDay).toString())
        assertEquals("Jun-Sep: Mo-Fr 09:00-17:00; Jun-Sep: Sa,Su 09:00-12:00", OpeningMonthsRow(juneToSeptember, mutableListOf(workweek, weekendLateMorning)).toString())
    }

    @Test fun `prepend months before every weekday`() {
        //comma here is ensured by test from "test tag generation"
        assertEquals("Jun-Sep: Mo,Tu 08:00-12:00, Jun-Sep: Tu-Fr 14:00-18:00", OpeningMonthsRow(juneToSeptember, mutableListOf(mondayTuesdayMorning, tuesdayToFridayAfternoon)).toString())
    }

    private fun months(vararg ranges: OpeningMonths) = ranges.toList()

    private fun times(vararg ranges: TimeRange) = ranges.toMutableList()

    private fun weekdays(vararg ranges: OpeningWeekdays) = ranges.toList()

    private fun clusters(vararg ranges: List<OpeningWeekdays>) = ranges.toList()
}
