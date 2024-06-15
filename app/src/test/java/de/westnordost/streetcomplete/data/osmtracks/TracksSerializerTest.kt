package de.westnordost.streetcomplete.data.osmtracks

import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class TracksSerializerTest {

    @Test
    fun `serialize to xml`() {
        val gpx = """
            <gpx xmlns="http://www.topografix.com/GPX/1/0" version="1.0" creator="${ApplicationConstants.USER_AGENT}">
            <trk>
            <trkseg>
            <trkpt lat="12.34" lon="56.78">
            <time>2024-06-05T09:51:14Z</time>
            <ele>1.23</ele>
            <hdop>4.56</hdop>
            </trkpt>
            <trkpt lat="12.3999" lon="56.7999">
            <time>2024-06-05T09:51:15Z</time>
            <ele>1.24</ele>
            <hdop>4.57</hdop>
            </trkpt>
            </trkseg>
            </trk>
            </gpx>
        """

        val track = listOf(
            Trackpoint(
                position = LatLon(12.34, 56.78),
                time = Instant.parse("2024-06-05T09:51:14Z").toEpochMilliseconds(),
                accuracy = 4.56f,
                elevation = 1.23f
            ),
            Trackpoint(
                position = LatLon(12.3999, 56.7999),
                time = Instant.parse("2024-06-05T09:51:15Z").toEpochMilliseconds(),
                accuracy = 4.57f,
                elevation = 1.24f
            ),
        )

        assertEquals(
            gpx.replace(Regex("[\n\r] *"), ""),
            TracksSerializer().serialize(track)
        )
    }
}
