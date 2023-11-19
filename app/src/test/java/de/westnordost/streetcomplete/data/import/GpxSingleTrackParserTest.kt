package de.westnordost.streetcomplete.data.import

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.testutils.any
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.on
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

private const val TRACK = "trk"
private const val TRACK_SEGMENT = "trkseg"
private const val TRACK_POINT = "trkpt"

class GpxSingleTrackParserTest {
    @Test
    fun `successfully parses minimal sample track`() {
        val originalTrackPoints = arrayListOf(
            createWaypoint("22.22", "172.3"),
            createWaypoint("39.11111", "-179.999"),
            createWaypoint("-25.312", "7"),
            createWaypoint("57.0", "123"),
            createWaypoint("-89.9999", "-12.02"),
            createWaypoint("-72.0", "0.3"),
        )
        assertSuccess(originalTrackPoints, parse(createGpxWithTrackPoints(originalTrackPoints)))
    }

    @Test
    fun `concatenates multiple track segments`() {
        val trackPointsSegment1 = arrayListOf(
            createWaypoint("-56.0", "0.0"),
            createWaypoint("57.57", "172.3")
        )
        val trackPointsSegment2 = arrayListOf(
            createWaypoint("-87.0", "-99.2"),
            createWaypoint("12.67", "132.29")
        )
        val gpx = MockXmlElement(
            "gpx", MockXmlElement(
                TRACK, arrayListOf(
                    MockXmlElement(TRACK_SEGMENT, trackPointsSegment1),
                    MockXmlElement(TRACK_SEGMENT, trackPointsSegment2)
                )
            )
        )
        assertSuccess(trackPointsSegment1 + trackPointsSegment2, parse(gpx))
    }

    @Test
    fun `discards further tracks`() {
        val trackPoints1 = arrayListOf(
            createWaypoint("-12.33", "0.0"),
            createWaypoint("74.1", "-122.34")
        )
        val trackPoints2 = arrayListOf(
            createWaypoint("-0.0", "-12"),
            createWaypoint("-90.0", "180.0")
        )
        val gpx = MockXmlElement(
            "gpx", arrayListOf(
                MockXmlElement(TRACK, MockXmlElement(TRACK_SEGMENT, trackPoints1)),
                MockXmlElement(TRACK, MockXmlElement(TRACK_SEGMENT, trackPoints2))
            )
        )
        assertSuccess(trackPoints1, parse(gpx))
    }

    @Test
    fun `throws on invalid trackPoints`() {
        assertFails {
            parse(createGpxWithTrackPoints(arrayListOf(createWaypoint("99.0", "-12.1")))).toList()
        }
        assertFails {
            parse(createGpxWithTrackPoints(arrayListOf(createWaypoint("-11.5", "-181.0")))).toList()
        }
    }

    @Test
    fun `throws on non-gpx files`() {
        assertFails { parse(MockXmlElement("xml", arrayListOf())).toList() }
    }

    @Test
    fun `handles additional data gracefully`() {
        val trackPoints = arrayListOf(createWaypoint("88", "-19"))
        val gpx = MockXmlElement(
            "gpx",
            hashMapOf("version" to "1.1", "creator" to ""),
            arrayListOf(
                MockXmlElement("metadata", arrayListOf(MockXmlElement("desc", arrayListOf()))),
                MockXmlElement(TRACK, MockXmlElement(TRACK_SEGMENT, trackPoints))
            )
        )

        assertSuccess(trackPoints, parse(gpx))
    }
}

/* helper functions */
private fun createGpxWithTrackPoints(trackPoints: MutableList<MockXmlElement>): MockXmlElement {
    return MockXmlElement(
        "gpx",
        hashMapOf("version" to "1.1", "creator" to ""),
        arrayListOf(MockXmlElement(TRACK, MockXmlElement(TRACK_SEGMENT, trackPoints)))
    )
}

private fun createWaypoint(lat: String, lon: String): MockXmlElement {
    return MockXmlElement(TRACK_POINT, mapOf("lat" to lat, "lon" to lon), ArrayList())
}

private fun parse(gpx: MockXmlElement): Sequence<LatLon> {
    val xpp: XmlPullParser = mock()
    on(xpp.next()).thenAnswer { gpx.next() }
    on(xpp.nextTag()).thenAnswer { gpx.next() }
    on(xpp.eventType).thenAnswer { gpx.getEventType() }
    on(xpp.name).thenAnswer { gpx.getName() }
    on(xpp.getAttributeValue(any(), anyString()))
        .thenAnswer { i -> gpx.getAttributeValue(i.getArgument(1)) }
    on(xpp.require(anyInt(), anyString(), anyString()))
        .thenAnswer { i ->
            val eventType: Int = i.getArgument(0)
            val name: String? = i.getArgument(2)
            if (eventType != gpx.getEventType() || (name != null && name != gpx.getName())) {
                throw XmlPullParserException("wrong eventType or name")
            }
        }
    return parseSingleGpxTrack(xpp)
}

private fun assertSuccess(
    originalTrackPoints: List<MockXmlElement>,
    parseResult: Sequence<LatLon>,
) {
    val result = parseResult.toList()
    assertEquals(
        originalTrackPoints.size, result.size,
        "All trackPoints are retrieved"
    )
    originalTrackPoints.zip(result).forEach { pair ->
        assertEquals(
            expected = pair.component1().getAttributeValue("lat")?.toDouble(),
            actual = pair.component2().latitude,
            "Latitudes match"
        )
        assertEquals(
            expected = pair.component1().getAttributeValue("lon")?.toDouble(),
            actual = pair.component2().longitude,
            "Longitudes match"
        )
    }
}

private class MockXmlElement(
    private val name: String,
    private val attributes: Map<String, String>,
    private val children: MutableList<MockXmlElement>,
) {
    constructor(name: String, child: MockXmlElement) : this(name, hashMapOf(), arrayListOf(child))
    constructor(name: String, children: MutableList<MockXmlElement>)
        : this(name, hashMapOf(), children)

    private enum class TagState { BEFORE, ON_START_TAG, ON_CHILD_TAG, ON_END_TAG }

    private var tagState = TagState.BEFORE
    private var currentChildIdx = 0

    fun next(): Int {
        when (tagState) {
            TagState.BEFORE -> {
                tagState = TagState.ON_START_TAG
                return XmlPullParser.START_TAG
            }

            TagState.ON_END_TAG -> {
                return XmlPullParser.END_DOCUMENT
            }

            else -> {
                if (currentChildIdx < children.size) {
                    if (children[currentChildIdx].tagState == TagState.ON_END_TAG) {
                        currentChildIdx++
                    }
                }
                return if (currentChildIdx < children.size) {
                    tagState = TagState.ON_CHILD_TAG
                    children[currentChildIdx].next()
                } else {
                    tagState = TagState.ON_END_TAG
                    XmlPullParser.END_TAG
                }
            }
        }
    }

    private fun currentElement(): MockXmlElement {
        return when (tagState) {
            TagState.BEFORE -> throw IllegalStateException("call next first")
            TagState.ON_START_TAG -> this
            TagState.ON_CHILD_TAG -> children[currentChildIdx].currentElement()
            TagState.ON_END_TAG -> this
        }
    }

    fun getEventType(): Int {
        return when (currentElement().tagState) {
            TagState.ON_START_TAG -> XmlPullParser.START_TAG
            TagState.ON_END_TAG -> XmlPullParser.END_TAG
            else -> throw IllegalStateException("current element should always be on START or END tag")
        }
    }

    fun getName(): String {
        return currentElement().name
    }

    fun getAttributeValue(name: String): String? {
        return currentElement().attributes[name]
    }
}
