package de.westnordost.streetcomplete.osm.opening_hours.model

import androidx.compose.ui.text.intl.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

class LocalizationUtilsTest {
    @Test fun `TimeRange toLocalizedString`() {
        val germany = Locale("de-DE")
        val finnish = Locale("fi")
        val us = Locale("en-US")


        assertEquals(
            "00:10–00:35",
            TimeRange(10, 35).toLocalizedString(germany)
        )
        assertEquals(
            "00:10–01:20+",
            TimeRange(10, 80, true).toLocalizedString(germany)
        )
        assertEquals(
            "00:00+",
            TimeRange(0, 0, true).toLocalizedString(germany)
        )

        assertEquals(
            "0.06–1.00",
            TimeRange(6, 60).toLocalizedString(finnish)
        )

        assertEquals(
            "12:00 AM–12:00 PM",
            TimeRange(0, 720).toLocalizedString(us)
        )
        assertEquals(
            "8:25 AM–8:25 PM",
            TimeRange(505, 1225).toLocalizedString(us)
        )

        assertEquals(
            "12:00 AM–12:00 AM",
            TimeRange(0, 0).toLocalizedString(us)
        )
        assertEquals(
            "12:00 AM–12:00 AM",
            TimeRange(0, 24 * 60).toLocalizedString(us)
        )

        assertEquals(
            "00:00–24:00",
            TimeRange(0, 0).toLocalizedString(germany)
        )
        assertEquals(
            "00:00–24:00",
            TimeRange(0, 24 * 60).toLocalizedString(germany)
        )
        assertEquals(
            "0.00–24.00",
            TimeRange(0, 24 * 60).toLocalizedString(finnish)
        )
    }
}
