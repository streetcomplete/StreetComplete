package de.westnordost.streetcomplete.osm.duration

import kotlin.test.Test
import kotlin.test.assertEquals

class DurationTest() {
    @Test fun toOsmValue() {
        assertEquals("1 minute", Duration(1.0, DurationUnit.MINUTES).toOsmValue())
        assertEquals("12 minutes", Duration(12.0, DurationUnit.MINUTES).toOsmValue())
        assertEquals("1 hour", Duration(1.0, DurationUnit.HOURS).toOsmValue())
        assertEquals("12 hours", Duration(12.0, DurationUnit.HOURS).toOsmValue())
        assertEquals("1 day", Duration(1.0, DurationUnit.DAYS).toOsmValue())
        assertEquals("12 days", Duration(12.0, DurationUnit.DAYS).toOsmValue())

        assertEquals("day", DurationUnit.DAYS.toOsmValue(
            usePlural = false
        ))
        assertEquals("days", DurationUnit.DAYS.toOsmValue(
            usePlural = true
        ))
        assertEquals("minute", DurationUnit.MINUTES.toOsmValue(
            usePlural = false
        ))
        assertEquals("hours", DurationUnit.HOURS.toOsmValue(
            usePlural = true
        ))
    }
}
