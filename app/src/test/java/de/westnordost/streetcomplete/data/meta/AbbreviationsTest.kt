package de.westnordost.streetcomplete.data.meta

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class AbbreviationsTest {
    @Test fun `capitalizes first letter`() {
        val abbr = Abbreviations(mapOf("str" to "straße"), Locale.GERMANY)
        assertEquals("Straße", abbr.getExpansion("str", true, true))
    }

    @Test fun `removes abbreviation dot`() {
        val abbr = Abbreviations(mapOf("str" to "straße"), Locale.GERMANY)
        assertEquals("Straße", abbr.getExpansion("str.", true, true))
    }

    @Test fun `ignores case`() {
        val abbr = Abbreviations(mapOf("sTr" to "Straße"), Locale.GERMANY)
        assertEquals("Straße", abbr.getExpansion("StR", true, true))
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
        val abbr = Abbreviations(mapOf("...str" to "Straat"), Locale.US)
        assertEquals("Straat", abbr.getExpansion("str", true, true))
    }

    @Test fun `get expansion of only first word`() {
        val abbr = Abbreviations(mapOf("^st" to "Saint"), Locale.US)
        assertNull(abbr.getExpansion("st.", false, false))
        assertEquals("Saint", abbr.getExpansion("st.", true, false))
    }

    @Test fun `get expansion of only last word`() {
        val abbr = Abbreviations(mapOf("str$" to "Straße"), Locale.US)
        assertNull(abbr.getExpansion("str", true, false))
        assertNull(abbr.getExpansion("str", true, true))
        assertEquals("Straße", abbr.getExpansion("str", false, true))
    }

    @Test fun `uses unicode`() {
        val abbr = Abbreviations(mapOf("бл" to "Блок"), Locale("ru", "RU"))
        assertEquals("Блок", abbr.getExpansion("бл", true, true))
    }

    @Test fun `locale dependent case`() {
        val abbr = Abbreviations(mapOf("бл" to "блок"), Locale("ru", "RU"))
        assertEquals("Блок", abbr.getExpansion("Бл", true, true))
    }

    @Test fun `finds abbreviation`() {
        val abbr = Abbreviations(mapOf("str" to "Straße"), Locale.GERMANY)
        assertFalse(abbr.containsAbbreviations("stri stra straße"))
        assertTrue(abbr.containsAbbreviations("stri str straße"))
    }
}
