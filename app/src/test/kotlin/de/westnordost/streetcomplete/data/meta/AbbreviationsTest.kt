package de.westnordost.streetcomplete.data.meta

import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AbbreviationsTest {
    @Test fun `removes abbreviation dot`() {
        val abbr = Abbreviations(mapOf("str" to "straße"), Locale.GERMANY)
        assertEquals("straße", abbr.getExpansion("str.", true, true))
    }

    @Test fun `ignores case`() {
        val abbr = Abbreviations(mapOf("sTr" to "straße"), Locale.GERMANY)
        assertEquals("straße", abbr.getExpansion("StR", true, true))
    }

    @Test fun `expects own word by default`() {
        val abbr = Abbreviations(mapOf("st" to "street"), Locale.US)
        assertNull(abbr.getExpansion("Hanswurst", true, true))
    }

    @Test fun `concatenable expansion`() {
        val abbr = Abbreviations(mapOf("...str" to "Straat"), Locale.US)
        assertEquals("Konigstraat", abbr.getExpansion("Konigstr", true, true))
    }

    @Test fun `concatenable expansion on end`() {
        val abbr = Abbreviations(mapOf("...str$" to "Straat"), Locale.US)
        assertEquals("Konigstraat", abbr.getExpansion("Konigstr", true, true))
    }

    @Test fun `concatenable works normally for non-concatenation`() {
        val abbr = Abbreviations(mapOf("...str" to "straat"), Locale.US)
        assertEquals("straat", abbr.getExpansion("str", true, true))
    }

    @Test fun `get expansion of only first word`() {
        val abbr = Abbreviations(mapOf("^st" to "saint"), Locale.US)
        assertNull(abbr.getExpansion("st.", false, false))
        assertEquals("saint", abbr.getExpansion("st.", true, false))
    }

    @Test fun `get expansion of only last word`() {
        val abbr = Abbreviations(mapOf("str$" to "straße"), Locale.US)
        assertNull(abbr.getExpansion("str", true, false))
        assertNull(abbr.getExpansion("str", true, true))
        assertEquals("straße", abbr.getExpansion("str", false, true))
    }

    @Test fun `uses unicode`() {
        val abbr = Abbreviations(mapOf("бл" to "блок"), Locale("ru", "RU"))
        assertEquals("блок", abbr.getExpansion("бл", true, true))
    }

    @Test fun `finds abbreviation`() {
        val abbr = Abbreviations(mapOf("str" to "Straße"), Locale.GERMANY)
        assertFalse(abbr.containsAbbreviations("stri stra straße"))
        assertTrue(abbr.containsAbbreviations("stri str straße"))
    }
}
