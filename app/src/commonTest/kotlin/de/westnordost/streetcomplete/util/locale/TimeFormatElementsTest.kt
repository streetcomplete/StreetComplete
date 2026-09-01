package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TimeFormatElementsTest {
    @Test fun de() {
        assertEquals(
            TimeFormatElements(),
            TimeFormatElements.of(Locale("de"))
        )
    }

    @Test fun en_US() {
        assertEquals(
            TimeFormatElements(clock12 = Clock12Elements("AM", "PM")),
            TimeFormatElements.of(Locale("en-US"))
        )
    }

    @Test fun fr_CA() {
        val actual = TimeFormatElements.of(Locale("fr-CA"))
        assertEquals(
            TimeFormatElements(),
            actual.copy(hourSeparator = ":")
        )
        assertTrue(actual.hourSeparator == ":" || actual.hourSeparator == " h ")
    }

    @Test fun es_PA() {
        val actual = TimeFormatElements.of(Locale("es-PA"))
        assertEquals(
            TimeFormatElements(clock12 = Clock12Elements("a. m.", "p. m.")),
            actual.copy(
                clock12 = actual.clock12?.let {
                    it.copy(am = it.am.replace(' ', ' '), pm = it.pm.replace(' ', ' '))
                }
            )
        )
    }

    @Test fun ko() {
        assertEquals(
            TimeFormatElements(clock12 = Clock12Elements("오전", "오후", true)),
            TimeFormatElements.of(Locale("ko"))
        )
    }

    @Test fun bg() {
        assertEquals(
            TimeFormatElements(),
            TimeFormatElements.of(Locale("bg"))
        )
    }

    @Test fun my() {
        val actual = TimeFormatElements.of(Locale("my"))
        assertEquals(
            TimeFormatElements(),
            actual.copy(zero = '0')
        )
    }

    @Test fun dz() {
        val actual = TimeFormatElements.of(Locale("dz"))
        assertEquals(
            TimeFormatElements(
                clock12 = Clock12Elements("སྔ་ཆ་", "ཕྱི་ཆ་"),
                hourSeparator = " སྐར་མ་ ",
                before = "ཆུ་ཚོད་"
            ),
            actual.copy(zero = '0')
        )
    }
}
