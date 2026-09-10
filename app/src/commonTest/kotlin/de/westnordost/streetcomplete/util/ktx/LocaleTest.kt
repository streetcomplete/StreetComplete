package de.westnordost.streetcomplete.util.ktx

import androidx.compose.ui.text.intl.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

class LocaleTest {
    @Test fun names() {
        val english = Locale("en")

        val germanInGermany = Locale("de-DE")
        assertEquals("German", germanInGermany.getDisplayLanguage(english))
        assertEquals("Germany", germanInGermany.getDisplayRegion(english))
        assertEquals(null, germanInGermany.getDisplayScript(english))

        val serbianInLatn = Locale("sr-Latn")
        assertEquals("Serbian", serbianInLatn.getDisplayLanguage(english))
        assertEquals(null, serbianInLatn.getDisplayRegion(english))
        assertEquals("Latin", serbianInLatn.getDisplayScript(english))
    }
}
