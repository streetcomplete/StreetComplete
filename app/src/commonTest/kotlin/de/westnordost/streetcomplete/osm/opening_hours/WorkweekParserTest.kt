package de.westnordost.streetcomplete.osm.opening_hours

import de.westnordost.osm_opening_hours.model.Weekday.*
import kotlin.test.Test
import kotlin.test.assertEquals

class WorkweekParserTest {
    @Test fun toWeekdaysSelectors() {
        assertEquals(listOf(Monday), "Mo".toWeekdaysSelectors())
        assertEquals(listOf(Tuesday), "Tu".toWeekdaysSelectors())
        assertEquals(listOf(Wednesday), "We".toWeekdaysSelectors())
        assertEquals(listOf(Thursday), "Th".toWeekdaysSelectors())
        assertEquals(listOf(Friday), "Fr".toWeekdaysSelectors())
        assertEquals(listOf(Saturday), "Sa".toWeekdaysSelectors())
        assertEquals(listOf(Sunday), "Su".toWeekdaysSelectors())

        assertEquals(listOf(Monday..Sunday), "Mo-Su".toWeekdaysSelectors())

        assertEquals(listOf(Monday, Sunday), "Mo,Su".toWeekdaysSelectors())
        assertEquals(listOf(Monday..Thursday, Sunday), "Mo-Th,Su".toWeekdaysSelectors())

        assertEquals(listOf(), "invalid".toWeekdaysSelectors())
        assertEquals(listOf(), "Mo-".toWeekdaysSelectors())
    }
}
