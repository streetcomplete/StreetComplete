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

        val serbianInCyrillic = Locale("sr-Cyrl")
        assertEquals("Serbian", serbianInCyrillic.getDisplayLanguage(english))
        assertEquals(null, serbianInCyrillic.getDisplayRegion(english))
        assertEquals("Cyrillic", serbianInCyrillic.getDisplayScript(english))
    }
}
