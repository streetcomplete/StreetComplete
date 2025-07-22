package de.westnordost.streetcomplete.util.ktx

import androidx.compose.ui.text.intl.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

class LocaleTest {
    @Test fun names() {
        val english = Locale("en")
        val germanInGermany = Locale("de-DE")

        assertEquals("German", germanInGermany.getLanguageName(english))
        assertEquals("Germany", germanInGermany.getRegionName(english))
        assertEquals(null, germanInGermany.getScriptName(english))

        val serbianInCyrillic = Locale("sr-Cyrl")
        assertEquals("Serbian", serbianInCyrillic.getLanguageName(english))
        assertEquals(null, serbianInCyrillic.getRegionName(english))
        assertEquals("Cyrillic", serbianInCyrillic.getScriptName(english))
    }
}
