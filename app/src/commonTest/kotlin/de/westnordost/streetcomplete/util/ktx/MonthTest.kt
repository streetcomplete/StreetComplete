package de.westnordost.streetcomplete.util.ktx

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.Month
import kotlin.test.Test
import kotlin.test.assertEquals

class MonthTest {
    @Test fun names() {
        val german = Locale("de")

        assertEquals("Januar", Month(1).getDisplayName(german))
        assertEquals("Dez", Month(12).getShortDisplayName(german))
        assertEquals("D", Month(12).getNarrowDisplayName(german))
    }
}
