package de.westnordost.streetcomplete.util.ktx

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class DateTimeTest {
    @Test fun `check parsing of ISO timestamp with offset`() {
        assertEquals(
            1196673330000, // == java.time.OffsetDateTime.parse("2007-12-03T10:15:30+01:00").toInstant().toEpochMilli()
            Instant.parse("2007-12-03T10:15:30+01:00").toEpochMilliseconds()
        )
    }

    @Test fun `check LocalDate round-trip`() {
        val s = "2010-04-16"
        assertEquals(s, LocalDate.parse(s).toString())
    }
}
