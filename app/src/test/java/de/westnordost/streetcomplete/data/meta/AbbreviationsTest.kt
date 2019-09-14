package de.westnordost.streetcomplete.data.meta

import org.junit.Test

import java.io.ByteArrayInputStream
import java.util.Locale

import org.junit.Assert.*

class AbbreviationsTest {
    @Test fun `capitalizes first letter`() {
        assertEquals("Straße", abbr("str: straße", Locale.GERMANY).getExpansion("str", true, true))
    }

    @Test fun `removes abbreviation dot`() {
        assertEquals("Straße", abbr("str: straße", Locale.GERMANY).getExpansion("str.", true, true))
    }

    @Test fun `ignores case`() {
        assertEquals("Straße", abbr("sTr: Straße", Locale.GERMANY).getExpansion("StR", true, true))
    }

    @Test fun `expects own word by default`() {
        assertNull(abbr("st: street").getExpansion("Hanswurst", true, true))
    }

    @Test fun `concatenable expansion`() {
        assertEquals("Konigstraat", abbr("...str: Straat").getExpansion("Konigstr", true, true))
    }

    @Test fun `concatenable exoabsuib on end`() {
        assertEquals("Konigstraat", abbr("...str$: Straat").getExpansion("Konigstr", true, true))
    }

    @Test fun `concatenable works normally for non-concatenation`() {
        assertEquals("Straat", abbr("...str: Straat").getExpansion("str", true, true))
    }

    @Test fun `get expansion of only first word`() {
        val abbr = abbr("^st: Saint")
        assertNull(abbr.getExpansion("st.", false, false))
        assertEquals("Saint", abbr.getExpansion("st.", true, false))
    }

    @Test fun `get expansion of only last word`() {
        val abbr = abbr("str$: Straße")
        assertNull(abbr.getExpansion("str", true, false))
        assertNull(abbr.getExpansion("str", true, true))
        assertEquals("Straße", abbr.getExpansion("str", false, true))
    }

    @Test fun `uses unicode`() {
        assertEquals("Блок", abbr("бл: Блок", Locale("ru", "RU")).getExpansion("бл", true, true))
    }

    @Test fun `locale dependent case`() {
        assertEquals("Блок", abbr("бл: блок", Locale("ru", "RU")).getExpansion("Бл", true, true))
    }

    @Test fun `finds abbreviation`() {
        assertFalse(abbr("str: Straße", Locale.GERMANY).containsAbbreviations("stri stra straße"))
        assertTrue(abbr("str: Straße", Locale.GERMANY).containsAbbreviations("stri str straße"))
    }

    @Test(expected = RuntimeException::class)
    fun `throws exception on invalid input`() {
        abbr("d:\n  - a\n  b: c\n")
    }

    private fun abbr(input: String, locale: Locale = Locale.US) =
        Abbreviations(ByteArrayInputStream(input.toByteArray(charset("UTF-8"))), locale)
}
