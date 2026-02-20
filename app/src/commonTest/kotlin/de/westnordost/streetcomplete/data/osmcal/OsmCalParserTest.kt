package de.westnordost.streetcomplete.data.osmcal

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.toInstant
import kotlinx.io.Buffer
import kotlinx.io.writeString
import kotlinx.serialization.SerializationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class OsmCalParserTest {

    @Test fun `parse minimal`() {
        val buffer = Buffer()
        buffer.writeString("""
            [
                {
                    "name": "Mapping Party #23",
                    "url": "https://osmcal.org/event/23/",
                    "date": {
                        "start": "2020-05-24T12:00:00+09:00",
                        "human": "24th May 12:00–14:00",
                        "human_short": "24th May",
                        "whole_day": false
                    },
                    "location": {
                        "coords": [135.5023, 34.6931]
                    }
                }
            ]
        """.trimIndent())
        assertEquals(
            listOf(CalendarEvent(
                id = 23L,
                name = "Mapping Party #23",
                startDate = LocalDateTime(2020, 5, 24, 12, 0).toInstant(UtcOffset(9)),
                endDate = null,
                wholeDay = false,
                position = LatLon(34.6931, 135.5023),
                venue = null,
                address = null,
                notified = false
            )),
            OsmCalParser().parse(buffer)
        )
    }

    @Test fun `parse full`() {
        val buffer = Buffer()
        buffer.writeString("""
            [
                {
                    "name": "Mapping Party #23",
                    "url": "https://osmcal.org/event/23/",
                    "date": {
                        "start": "2020-05-24T12:00:00+09:00",
                        "end": "2020-05-24T14:00:00+09:00",
                        "human": "24th May 12:00–14:00",
                        "human_short": "24th May",
                        "whole_day": false
                    },
                    "location": {
                        "short": "Osaka, Japan",
                        "detailed": "Tosabori-dori, Chuo, Osaka, Japan",
                        "coords": [135.5023, 34.6931],
                        "venue": "Cool Pub"
                    },
                    "cancelled": false
                }
            ]
        """.trimIndent())
        assertEquals(
            listOf(CalendarEvent(
                id = 23L,
                name = "Mapping Party #23",
                startDate = LocalDateTime(2020, 5, 24, 12, 0).toInstant(UtcOffset(9)),
                endDate = LocalDateTime(2020, 5, 24, 14, 0).toInstant(UtcOffset(9)),
                wholeDay = false,
                position = LatLon(34.6931, 135.5023),
                venue = "Cool Pub",
                address = "Tosabori-dori, Chuo, Osaka, Japan",
                notified = false
            )),
            OsmCalParser().parse(buffer)
        )
    }

    @Test fun `parse ignored`() {
        val buffer = Buffer()
        buffer.writeString("""
            [
                {
                    "name": "no location",
                    "url": "https://osmcal.org/event/1/",
                    "date": {
                        "start": "2020-05-24T12:00:00+09:00",
                        "human": "24th May 12:00–14:00",
                        "human_short": "24th May",
                        "whole_day": false
                    }
                },
                {
                    "name": "cancelled",
                    "url": "https://osmcal.org/event/3/",
                    "date": {
                        "start": "2020-05-24T12:00:00+09:00",
                        "human": "24th May 12:00–14:00",
                        "human_short": "24th May",
                        "whole_day": false
                    },
                    "location": {
                        "coords": [135.5023, 34.6931]
                    },
                    "cancelled": true
                }
            ]
        """.trimIndent())
        assertTrue(OsmCalParser().parse(buffer).isEmpty())
    }

    @Test fun `parse error`() {
        val buffer = Buffer()
        buffer.writeString("""
            [
                {
                    "name": "unknown url schema",
                    "url": "https://otherwebsite.org/event/2/",
                    "date": {
                        "start": "2020-05-24T12:00:00+09:00",
                        "human": "24th May 12:00–14:00",
                        "human_short": "24th May",
                        "whole_day": false
                    },
                    "location": {
                        "coords": [135.5023, 34.6931]
                    }
                }
            ]
        """.trimIndent())
        assertFailsWith(SerializationException::class) {
            OsmCalParser().parse(buffer)
        }
    }
}
