package de.westnordost.streetcomplete.util.ktx

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.DayOfWeek
import kotlin.test.Test
import kotlin.test.assertEquals

class DayOfWeekTest {
    @Test fun names() {
        val german = Locale("de")

        assertEquals("Montag", DayOfWeek(1).getDisplayName(german))
        assertEquals("Fr", DayOfWeek(5).getShortDisplayName(german))
        assertEquals("M", DayOfWeek(1).getNarrowDisplayName(german))
    }
}
