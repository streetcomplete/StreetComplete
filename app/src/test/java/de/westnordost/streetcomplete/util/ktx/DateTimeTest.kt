package de.westnordost.streetcomplete.util.ktx

import org.junit.Assert.assertEquals
import org.junit.Test

class DateTimeTest {
    @Test fun `check parsing of ISO timestamp with offset`() {
        assertEquals(
            java.time.OffsetDateTime.parse("2007-12-03T10:15:30+01:00").toInstant().toEpochMilli(),
            Instant.parse("2007-12-03T10:15:30+01:00").toEpochMilli()
        )
    }

    @Test fun `check LocalDate round-trip`() {
        val s = "2010-04-16"
        assertEquals(s, LocalDate.parse(s).toString())
    }
}
