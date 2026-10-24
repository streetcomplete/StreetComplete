package de.westnordost.streetcomplete

import androidx.compose.ui.text.intl.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

class AppLocaleTest {
    private val languageTags = listOf("en", "de", "fr", "zh-Hans", "zh-Hant", "sr-Cyrl", "sr-Latn")

    private fun resolve(locale: Locale?, systemLocales: List<Locale>): Locale =
        resolveAppLocale(locale, systemLocales, languageTags)

    @Test fun `obsolete scriptless choices fall back to the system locale`() {
        val systemLocale = Locale("de-DE")
        for (tag in listOf("zh", "zh-CN", "zh-TW", "zh-HK", "sr", "sr-RS")) {
            assertEquals(systemLocale, resolve(Locale(tag), listOf(systemLocale)), tag)
        }
    }

    @Test fun `explicit scripts and regions are preserved`() {
        for (tag in listOf("zh-Hans", "zh-Hant-TW", "sr-Latn-ME", "en-GB", "fr-CA")) {
            val locale = Locale(tag)
            assertEquals(locale, resolve(locale, listOf(Locale("de-DE"))), tag)
        }
    }

    @Test fun `ambiguous system locales fall back to the next locale or English`() {
        val systemLocales = listOf("zh-CN", "sr", "fr-CA", "de-DE").map { Locale(it) }
        assertEquals(Locale("fr-CA"), resolve(null, systemLocales))
        assertEquals(Locale("fr-CA"), resolve(Locale("zz"), systemLocales))
        assertEquals(Locale("zh-Hant-TW"), resolve(null, listOf(Locale("zh-Hant-TW"))))
        assertEquals(Locale("en"), resolve(Locale("zh-CN"), listOf(Locale("sr"))))
        assertEquals(Locale("en"), resolve(null, emptyList()))
    }
}
