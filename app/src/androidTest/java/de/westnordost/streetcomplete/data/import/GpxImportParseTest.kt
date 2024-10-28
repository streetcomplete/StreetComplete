package de.westnordost.streetcomplete.data.import

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class GpxImportParseTest {
    @Test
    fun successfullyParsesMinimalSampleTrack() = runBlocking {
        val originalTrackPoints = arrayListOf(
            TrackPoint("22.22", "172.3"),
            TrackPoint("39.11111", "-179.999"),
            TrackPoint("-25.312", "7"),
            TrackPoint("57.0", "123"),
            TrackPoint("-89.9999", "-12.02"),
            TrackPoint("-72.0", "0.3"),
        )

        val inputGpx =
            minimalGpxBuilder(originalTrackPoints)

        assertSuccess(
            originalTrackPoints,
            parseGpx(inputGpx)
        )
    }

    @Test
    fun concatenatesMultipleTrackSegments() = runBlocking {
        val trackPointsSegment1 = arrayListOf(
            TrackPoint("-56.0", "0.0"),
            TrackPoint("57.57", "172.3")
        )
        val trackPointsSegment2 = arrayListOf(
            TrackPoint("-87.0", "-99.2"),
            TrackPoint("12.67", "132.29")
        )

        val inputGpx = buildString {
            append("<gpx version='1.1' xmlns='http://www.topografix.com/GPX/1/1'>")
            append("<trk>")
            append("<trkseg>")
            trackPointsSegment1.forEach {
                append("<trkpt lat='${it.lat}' lon='${it.lon}'/>")
            }
            append("</trkseg>")
            append("<trkseg>")
            trackPointsSegment2.forEach {
                append("<trkpt lat='${it.lat}' lon='${it.lon}'/>")
            }
            append("</trkseg>")
            append("</trk>")
            append("</gpx>")
        }

        assertSuccess(
            trackPointsSegment1 + trackPointsSegment2,
            parseGpx(inputGpx)
        )
    }

    @Test
    fun processesMultipleTracksAndSegments() = runBlocking {
        val trackPoints1 = arrayListOf(
            TrackPoint("-12.33", "0.0"),
            TrackPoint("74.1", "-122.34")
        )
        val trackPoints2 = arrayListOf(
            TrackPoint("-0.0", "-12"),
            TrackPoint("-90.0", "180.0")
        )
        val trackPoints3 = arrayListOf(
            TrackPoint("11.1", "-92"),
            TrackPoint("90", "-0.0")
        )

        val inputGpx = buildString {
            append("<gpx version='1.1' xmlns='http://www.topografix.com/GPX/1/1'>")
            append("<trk>")
            append("<trkseg>")
            trackPoints1.forEach {
                append("<trkpt lat='${it.lat}' lon='${it.lon}'/>")
            }
            append("</trkseg>")
            append("<trkseg>")
            trackPoints2.forEach {
                append("<trkpt lat='${it.lat}' lon='${it.lon}'/>")
            }
            append("</trkseg>")
            append("</trk>")
            append("<trk>")
            append("<trkseg>")
            trackPoints3.forEach {
                append("<trkpt lat='${it.lat}' lon='${it.lon}'/>")
            }
            append("</trkseg>")
            append("</trk>")
            append("</gpx>")
        }

        assertSuccess(
            trackPoints1 + trackPoints2 + trackPoints3,
            parseGpx(inputGpx)
        )
    }

    @Test
    fun throwsOnInvalidTrackPoints(): Unit = runBlocking {
        assertFails {
            parseGpx(
                minimalGpxBuilder(
                    listOf(TrackPoint("99.0", "-12.1"))
                )
            )
        }
        assertFails {
            parseGpx(
                minimalGpxBuilder(
                    listOf(TrackPoint("-11.5", "-181.0"))
                )
            )
        }
    }

    @Test
    fun throwsOnNonGpxFiles(): Unit = runBlocking {
        val nonGpxXml = """
            <xml>
            </xml>
        """.trimIndent()
        assertFails {
            parseGpx(nonGpxXml)
        }
    }

    @Test
    fun exhaustingOuterBeforeInnerFlowYieldsNoElements() = runBlocking {
        val inputGpx = minimalGpxBuilder(
            arrayListOf(
                TrackPoint("-39.654", "180"),
                TrackPoint("90.0", "-180")
            )
        )

        // exhausting outer first
        val incorrectlyRetrievedSegments = parseGpxFile(inputGpx.byteInputStream()).toList()
        assertEquals(
            1, incorrectlyRetrievedSegments.size,
            "Exhausting outer first fails to retrieve the track segment"
        )
        assertEquals(
            emptyList(), incorrectlyRetrievedSegments.first().toList(),
            "Exhausting outer first unexpectedly yields track points"
        )

        // exhausting inner first
        val correctlyRetrievedSegments = parseGpx(inputGpx)
        assertEquals(
            2, correctlyRetrievedSegments.size,
            "Exhausting inner first fails to retrieve track points"
        )
    }

    @Test
    fun handlesAdditionalDataGracefully() = runBlocking {
        val originalTrackPoints =
            arrayListOf(TrackPoint("88", "-19"))

        val inputGpx = buildString {
            append("<gpx version='1.1' xmlns='http://www.topografix.com/GPX/1/1'>")
            append("<metadata>")
            append("<desc>Some GPS track</desc>")
            append("</metadata>")
            append("<trk>")
            append("<trkseg>")
            originalTrackPoints.forEach {
                append("<trkpt lat='${it.lat}' lon='${it.lon}'/>")
            }
            append("</trkseg>")
            append("</trk>")
            append("</gpx>")
        }

        assertSuccess(
            originalTrackPoints,
            parseGpx(inputGpx)
        )
    }

    private fun assertSuccess(
        originalTrackPoints: List<TrackPoint>,
        parseResult: List<LatLon>,
    ) {
        assertEquals(
            originalTrackPoints.size, parseResult.size,
            "Not all trackPoints are retrieved"
        )
        originalTrackPoints.map { it.toLatLon() }.zip(parseResult).forEach { pair ->
            assertEquals(
                expected = pair.component1().latitude,
                actual = pair.component2().latitude,
                "Latitudes don't match"
            )
            assertEquals(
                expected = pair.component1().longitude,
                actual = pair.component2().longitude,
                "Longitudes don't match"
            )
        }
    }
}
