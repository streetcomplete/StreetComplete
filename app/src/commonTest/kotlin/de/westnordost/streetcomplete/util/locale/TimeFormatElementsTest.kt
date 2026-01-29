package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

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
        assertEquals(
            TimeFormatElements(hourSeparator = " h "),
            TimeFormatElements.of(Locale("fr-CA"))
        )
    }

    @Test fun es_PA() {
        assertEquals(
            TimeFormatElements(clock12 = Clock12Elements("a. m.", "p. m.")),
            TimeFormatElements.of(Locale("es-PA"))
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
            TimeFormatElements(after = "ч."),
            TimeFormatElements.of(Locale("bg"))
        )
    }

    @Test fun my() {
        assertEquals(
            TimeFormatElements(),
            TimeFormatElements.of(Locale("my"))
        )
    }

    @Test fun dz() {
        assertEquals(
            TimeFormatElements(
                clock12 = Clock12Elements("སྔ་ཆ་", "ཕྱི་ཆ་"),
                hourSeparator = " སྐར་མ་ ",
                before = "ཆུ་ཚོད་"
            ),
            TimeFormatElements.of(Locale("dz"))
        )
    }
}
