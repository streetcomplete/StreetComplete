package de.westnordost.streetcomplete.util.ktx

import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.util.locale.DateTimeTextSymbolStyle.*
import kotlinx.datetime.DayOfWeek
import kotlin.test.Test
import kotlin.test.assertEquals

class DayOfWeekTest {
    @Test fun names() {
        val german = Locale("de")

        assertEquals("Montag", DayOfWeek(1).getDisplayName(Full, german))
        assertEquals("Fr", DayOfWeek(5).getDisplayName(Short, german))
        assertEquals("M", DayOfWeek(1).getDisplayName(Narrow, german))
    }
}
