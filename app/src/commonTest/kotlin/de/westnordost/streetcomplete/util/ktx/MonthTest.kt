package de.westnordost.streetcomplete.util.ktx

import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.util.locale.DateTimeTextSymbolStyle
import kotlinx.datetime.Month
import kotlin.test.Test
import kotlin.test.assertEquals

class MonthTest {
    @Test fun names() {
        val german = Locale("de")

        assertEquals("Januar", Month(1).getDisplayName(DateTimeTextSymbolStyle.Full, german))
        assertEquals("Dez", Month(12).getDisplayName(DateTimeTextSymbolStyle.Short, german))
        assertEquals("D", Month(12).getDisplayName(DateTimeTextSymbolStyle.Narrow, german))
    }
}
