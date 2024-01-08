package de.westnordost.streetcomplete.data.logs

import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals

class LogMessageTest {
    @Test fun `toString is as expected`() {
        assertEquals(
            "[TAG] test message error",
            LogMessage(
                LogLevel.ERROR,
                "TAG",
                "test message",
                "error",
                1000
            ).toString()
        )
    }

    @Test fun `format is as expected`() {
        val m1 = LogMessage(
            LogLevel.ERROR,
            "TAG",
            "test message",
            "error",
            1000 * 60 * 30
        )
        val m2 = LogMessage(
            LogLevel.ERROR,
            "TAG",
            "test message",
            "error",
            1000 * 60 * 60
        )

        assertEquals(
            "1970-01-01T00:30: [TAG] test message error\n" +
                "1970-01-01T01:00: [TAG] test message error",
            listOf(m1, m2).format(tz = TimeZone.UTC)
        )
    }
}
