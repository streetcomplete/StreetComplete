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
    private val tuesday = Weekdays(booleanArrayOf(false, true))

    private val morning = TimeRange(8 * 60, 12 * 60, false)
    private val midday = TimeRange(10 * 60, 16 * 60, false)
    private val afternoon = TimeRange(14 * 60, 18 * 60, false)
    private val longAfternoon = TimeRange(13 * 60, 20 * 60, false)
    private val duskTillDawn = TimeRange(18 * 60, 6 * 60, false)
    private val earlyMorning = TimeRange(4 * 60, 8 * 60, false)

    private val mondayMorning = OpeningWeekdaysRow(monday, morning)

    private val wholeYear = CircularSection(0, 11)
    private val januaryToJune = CircularSection(0, 6)
    private val julyToDecember = CircularSection(6, 11)

    @Test fun `copies opening months`() {
        val actual = listOf(
            OpeningMonthsRow(januaryToJune, mondayMorning),
            OpeningMonthsRow(julyToDecember, mondayMorning)
        ).toOpeningMonthsList()

        val expected = months(
            OpeningMonths(januaryToJune, clusters(weekdays(OpeningWeekdays(monday, times(morning))))),
            OpeningMonths(julyToDecember, clusters(weekdays(OpeningWeekdays(monday, times(morning)))))
        )

        assertEquals(expected, actual)
    }

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

    private fun months(vararg ranges: OpeningMonths) = ranges.toList()

    private fun times(vararg ranges: TimeRange) = ranges.toMutableList()

    private fun weekdays(vararg ranges: OpeningWeekdays) = ranges.toList()

    private fun clusters(vararg ranges: List<OpeningWeekdays>) = ranges.toList()
}
