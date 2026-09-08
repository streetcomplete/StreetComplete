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
        val expected = TimeFormatElements()
        val actual = TimeFormatElements.of(Locale("fr-CA"))

        assertEquals(expected.clock12, actual.clock12)
        assertTrue(actual.hourSeparator in listOf(" h ", ":")) // differs per platform
        assertEquals(expected.before, actual.before)
        assertEquals(expected.after, actual.after)
        assertEquals(expected.zero, actual.zero)
    }

    @Test fun es_PA() {
        val expected = TimeFormatElements()
        val actual = TimeFormatElements.of(Locale("es-PA"))

        assertTrue(actual.clock12 in listOf(
            Clock12Elements("a. m.", "p. m."),
            Clock12Elements("a. m.", "p. m.")
        )) // differs per platform
        assertEquals(expected.hourSeparator, actual.hourSeparator)
        assertEquals(expected.before, actual.before)
        assertEquals(expected.after, actual.after)
        assertEquals(expected.zero, actual.zero)
    }

    @Test fun ko() {
        assertEquals(
            TimeFormatElements(clock12 = Clock12Elements("오전", "오후", true)),
            TimeFormatElements.of(Locale("ko"))
        )
    }

    @Test fun bg() {
        val expected = TimeFormatElements()
        val actual = TimeFormatElements.of(Locale("bg"))

        assertEquals(expected.clock12, actual.clock12)
        assertEquals(expected.hourSeparator, actual.hourSeparator)
        assertEquals(expected.before, actual.before)
        assertTrue(actual.after in listOf("ч.", "")) // differs per platform
        assertEquals(expected.zero, actual.zero)
    }

    @Test fun my() {
        val expected = TimeFormatElements()
        val actual = TimeFormatElements.of(Locale("my"))

        assertEquals(expected.clock12, actual.clock12)
        assertEquals(expected.hourSeparator, actual.hourSeparator)
        assertEquals(expected.before, actual.before)
        assertEquals(expected.after, actual.after)
        assertTrue(actual.zero in listOf('0', '၀')) // differs per platform
    }

    @Test fun dz() {
        val expected = TimeFormatElements(
            clock12 = Clock12Elements("སྔ་ཆ་", "ཕྱི་ཆ་"),
            hourSeparator = " སྐར་མ་ ",
            before = "ཆུ་ཚོད་",
            after = "",
        )
        val actual = TimeFormatElements.of(Locale("dz"))

        assertEquals(expected.clock12, actual.clock12)
        assertEquals(expected.hourSeparator, actual.hourSeparator)
        assertEquals(expected.before, actual.before)
        assertEquals(expected.after, actual.after)
        assertTrue(actual.zero in listOf('0', '༠')) // differs per platform
    }
}
