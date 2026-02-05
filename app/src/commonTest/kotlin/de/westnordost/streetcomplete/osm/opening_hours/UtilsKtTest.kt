package de.westnordost.streetcomplete.osm.opening_hours

import de.westnordost.osm_opening_hours.model.CalendarDate
import de.westnordost.osm_opening_hours.model.Month
import de.westnordost.osm_opening_hours.model.MonthRange
import de.westnordost.osm_opening_hours.model.MonthsOrDateSelector
import de.westnordost.osm_opening_hours.model.Nth
import de.westnordost.osm_opening_hours.model.SingleMonth
import de.westnordost.osm_opening_hours.model.SpecificWeekdays
import de.westnordost.osm_opening_hours.model.Weekday
import de.westnordost.osm_opening_hours.model.WeekdaysSelector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UtilsKtTest {
    @Test fun toOrdinalRanges() {
        val a = 'a'
        val b = 'b'
        val c = 'c'
        val d = 'd'
        val all = listOf(a, b, c, d)

        assertEquals(
            listOf(),
            setOf<Char>().toOrdinalRanges(all)
        )

        assertEquals(
            listOf(),
            setOf(a).toOrdinalRanges(listOf())
        )

        assertEquals(
            listOf(0..0),
            setOf(a).toOrdinalRanges(all)
        )

        assertEquals(
            listOf(0..1),
            setOf(a, b).toOrdinalRanges(all)
        )

        assertEquals(
            listOf(0..0, 2..2),
            setOf(a, c).toOrdinalRanges(all)
        )

        assertEquals(
            listOf(0..3),
            setOf(a, b, c, d).toOrdinalRanges(all)
        )

        assertEquals(
            listOf(3..0),
            setOf(a, d).toOrdinalRanges(all)
        )

        assertEquals(
            listOf(1..1, 3..3),
            setOf(b, d).toOrdinalRanges(all)
        )

        assertEquals(
            listOf(3..1),
            setOf(a, b, d).toOrdinalRanges(all)
        )
    }

    @Test fun getMonths() {
        assertFailsWith(UnsupportedOperationException::class) {
            listOf<MonthsOrDateSelector>(CalendarDate(Month.May, 1)).getMonths()
        }

        assertFailsWith(UnsupportedOperationException::class) {
            listOf(SingleMonth(1995, Month.May)).getMonths()
        }

        assertFailsWith(UnsupportedOperationException::class) {
            listOf(MonthRange(1995, Month.May, Month.June)).getMonths()
        }

        assertEquals(
            setOf(),
            listOf<MonthsOrDateSelector>().getMonths()
        )

        assertEquals(
            setOf(Month.May),
            listOf(SingleMonth(Month.May)).getMonths()
        )

        assertEquals(
            setOf(Month.May, Month.July),
            listOf(SingleMonth(Month.May), SingleMonth(Month.July)).getMonths()
        )

        assertEquals(
            setOf(Month.May, Month.June, Month.July),
            listOf(Month.May..Month.July).getMonths()
        )

        assertEquals(
            setOf(Month.May, Month.June, Month.July, Month.March),
            listOf(Month.May..Month.July, SingleMonth(Month.March)).getMonths()
        )
    }

    @Test fun toMonthsSelectors() {
        assertEquals(
            listOf(),
            setOf<Month>().toMonthsSelectors()
        )

        assertEquals(
            listOf(SingleMonth(Month.May)),
            setOf(Month.May).toMonthsSelectors()
        )

        assertEquals(
            listOf(SingleMonth(Month.May), SingleMonth(Month.August)),
            setOf(Month.May, Month.August).toMonthsSelectors()
        )

        assertEquals(
            listOf(SingleMonth(Month.May), SingleMonth(Month.June)),
            setOf(Month.May, Month.June).toMonthsSelectors()
        )

        assertEquals(
            listOf(Month.May .. Month.July),
            setOf(Month.May, Month.June, Month.July).toMonthsSelectors()
        )

        assertEquals(
            listOf(SingleMonth(Month.December), SingleMonth(Month.January)),
            setOf(Month.January, Month.December).toMonthsSelectors()
        )

        assertEquals(
            listOf(Month.December .. Month.February),
            setOf(Month.January, Month.February, Month.December).toMonthsSelectors()
        )
    }

    @Test fun getWeekdays() {
        assertFailsWith(UnsupportedOperationException::class) {
            listOf<WeekdaysSelector>(SpecificWeekdays(Weekday.Monday, listOf(Nth(1)))).getWeekdays()
        }

        assertEquals(
            setOf(),
            listOf<WeekdaysSelector>().getWeekdays()
        )

        assertEquals(
            setOf(Weekday.Monday),
            listOf(Weekday.Monday).getWeekdays()
        )

        assertEquals(
            setOf(Weekday.Monday, Weekday.Friday),
            listOf(Weekday.Monday, Weekday.Friday).getWeekdays()
        )

        assertEquals(
            setOf(Weekday.Monday, Weekday.Tuesday, Weekday.Wednesday),
            listOf(Weekday.Monday..Weekday.Wednesday).getWeekdays()
        )

        assertEquals(
            setOf(Weekday.Monday, Weekday.Tuesday, Weekday.Wednesday, Weekday.Friday),
            listOf(Weekday.Monday..Weekday.Wednesday, Weekday.Friday).getWeekdays()
        )
    }

    @Test fun toWeekdaysSelectors() {
        assertEquals(
            listOf(),
            setOf<Weekday>().toWeekdaysSelectors()
        )

        assertEquals(
            listOf(Weekday.Monday),
            setOf(Weekday.Monday).toWeekdaysSelectors()
        )

        assertEquals(
            listOf(Weekday.Monday, Weekday.Wednesday),
            setOf(Weekday.Monday, Weekday.Wednesday).toWeekdaysSelectors()
        )

        assertEquals(
            listOf(Weekday.Monday, Weekday.Tuesday),
            setOf(Weekday.Monday, Weekday.Tuesday).toWeekdaysSelectors()
        )

        assertEquals(
            listOf(Weekday.Monday .. Weekday.Wednesday),
            setOf(Weekday.Monday, Weekday.Tuesday, Weekday.Wednesday).toWeekdaysSelectors()
        )

        assertEquals(
            listOf(Weekday.Sunday, Weekday.Monday),
            setOf(Weekday.Monday, Weekday.Sunday).toWeekdaysSelectors()
        )

        assertEquals(
            listOf(Weekday.Saturday .. Weekday.Monday),
            setOf(Weekday.Monday, Weekday.Saturday, Weekday.Sunday).toWeekdaysSelectors()
        )
    }
}
