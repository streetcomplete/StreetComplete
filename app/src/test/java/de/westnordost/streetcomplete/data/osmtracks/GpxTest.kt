package de.westnordost.streetcomplete.data.osmtracks

import kotlinx.datetime.Instant
import nl.adaptivity.xmlutil.serialization.XML
import kotlin.test.Test
import kotlin.test.assertEquals

class GpxTest {
    // only testing a gpx with one track and one segment, because that is the only thing we use
    private val xml = """
        <gpx xmlns="http://www.topografix.com/GPX/1/0" version="1.0" creator="ABC">
        <trk>
        <trkseg>
        <trkpt lat="12.34" lon="56.78">
        <time>2024-06-05T09:51:14Z</time>
        <ele>1.23</ele>
        <hdop>4.56</hdop>
        </trkpt>
        <trkpt lat="12.3999" lon="56.7999" />
        </trkseg>
        </trk>
        </gpx>
    """

    private val gpx = Gpx(
        version = 1.0f,
        creator = "ABC",
        tracks = listOf(GpsTrack(segments = listOf(GpsTrackSegment(points = listOf(
            GpsTrackPoint(
                lat = 12.34,
                lon = 56.78,
                time = Instant.parse("2024-06-05T09:51:14Z"),
                ele = 1.23f,
                hdop = 4.56f,
            ),
            GpsTrackPoint(
                lat = 12.3999,
                lon = 56.7999,
            ),
        )))))
    )

    @Test
    fun `serialize to xml`() {
        assertEquals(
            xml.trimIndent().replace(Regex("[\n\r]"), ""),
            XML.encodeToString(gpx)
        )
    }

    @Test
    fun `deserialize from xml`() {
        assertEquals(gpx, XML.decodeFromString(xml),)
    }
}
