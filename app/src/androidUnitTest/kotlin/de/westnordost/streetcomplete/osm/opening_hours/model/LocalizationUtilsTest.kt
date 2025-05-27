package de.westnordost.streetcomplete.osm.opening_hours.model

import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

class LocalizationUtilsTest {
    @Test fun `TimeRange toLocalizedString`() {
        val openEnd = TimeRange(10, 80, true)

        assertEquals(
            "00:10–01:20+",
            openEnd.toLocalizedString(Locale.GERMANY)
        )
        assertEquals(
            "00:10–01:20+",
            openEnd.toLocalizedString(Locale.GERMANY)
        )
        assertEquals(
            "00:00+",
            TimeRange(0, 0, true).toLocalizedString(Locale.GERMANY)
        )

        assertEquals(
            "12:00 AM–12:00 PM",
            TimeRange(0, 720).toLocalizedString(Locale.US)
        )
        assertEquals(
            "8:25 AM–8:25 PM",
            TimeRange(505, 1225).toLocalizedString(Locale.US)
        )

        assertEquals(
            "12:00 AM–12:00 AM",
            TimeRange(0, 0).toLocalizedString(Locale.US)
        )
        assertEquals(
            "12:00 AM–12:00 AM",
            TimeRange(0, 24 * 60).toLocalizedString(Locale.US)
        )

        assertEquals(
            "00:00–24:00",
            TimeRange(0, 0).toLocalizedString(Locale.GERMANY)
        )
        assertEquals(
            "00:00–24:00",
            TimeRange(0, 24 * 60).toLocalizedString(Locale.GERMANY)
        )
    }
}
