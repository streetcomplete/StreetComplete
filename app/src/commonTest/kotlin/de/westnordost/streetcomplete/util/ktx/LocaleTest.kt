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

        // Use a non-default script: Foundation canonicalizes sr-Cyrl to sr on Apple targets.
        val serbianInLatin = Locale("sr-Latn")
        assertEquals("Serbian", serbianInLatin.getDisplayLanguage(english))
        assertEquals(null, serbianInLatin.getDisplayRegion(english))
        assertEquals("Latin", serbianInLatin.getDisplayScript(english))
    }
}
