package de.westnordost.streetcomplete.data.import

import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilePos
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilesRect
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.math.contains
import de.westnordost.streetcomplete.util.math.initialBearingTo
import de.westnordost.streetcomplete.util.math.translate
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class GpxImportTest {
    @Test
    fun `download works on single-segment track`() = runBlocking {
        val originalTrackPoints = arrayListOf(
            TrackPoint("0.0", "0.0"),
            TrackPoint("1.3", "-0.3"),
            TrackPoint("2", "-2"),
            TrackPoint("2.4", "-2.2"),
            TrackPoint("2.4", "-2.2"),
            TrackPoint("2.6", "-3"),
        )

        val minDownloadDistance = 100.0
        val inputGpx = minimalGpxBuilder(originalTrackPoints)
        val data = importGpx(
            inputGpx.byteInputStream(),
            displayTrack = true,
            findDownloadBBoxes = true,
            minDownloadDistance
        )
        assertInvariants(originalTrackPoints, data.getOrThrow(), minDownloadDistance)
    }

    @Test
    fun `display-only import works on single-segment track`() = runBlocking {
        val originalTrackPoints = arrayListOf(
            TrackPoint("-36.1", "-143.0"),
            TrackPoint("-40.2", "-179.999"),
            TrackPoint("-42.0", "179"),
            TrackPoint("-38.38", "171"),
        )

        val inputGpx = minimalGpxBuilder(originalTrackPoints)
        val data = importGpx(
            inputGpx.byteInputStream(),
            displayTrack = true,
            findDownloadBBoxes = false,
            250.0
        )

        originalTrackPoints.forEach { trackPoint ->
            assertTrue(
                data.getOrThrow().segments.any { it.contains(trackPoint.toLatLon()) },
                "originalTrackPoint $trackPoint not contained in displayed segments"
            )
        }
    }

    @Test
    fun `east-west line downloads necessary area`() = runBlocking {
        // at zoom level 16, one tile is 0.005° latitude high, or ~500m high on equator
        // the equator itself lies directly on a boundary between two tile rows
        // LatLon(0.0025, ..) is in the middle of the row above the equator
        require(ApplicationConstants.DOWNLOAD_TILE_ZOOM == 16)
        val originalTrackPoints = arrayListOf(
            TrackPoint("0.0025", "-70.0"),
            TrackPoint("0.0025", "-71.0"),
        )
        val inputGpx = minimalGpxBuilder(originalTrackPoints)
        val centerRowY =
            originalTrackPoints[0].toLatLon()
                .enclosingTilePos(ApplicationConstants.DOWNLOAD_TILE_ZOOM).y

        // setting the minDownloadDistance well below half tile height should download only one row
        val smallMinDownloadDistance = 100.0
        val smallData = importGpx(
            inputGpx.byteInputStream(),
            displayTrack = true,
            findDownloadBBoxes = true,
            smallMinDownloadDistance
        ).getOrThrow()
        assertInvariants(originalTrackPoints, smallData, smallMinDownloadDistance)
        smallData.downloadBBoxes
            .map { it.enclosingTilesRect(ApplicationConstants.DOWNLOAD_TILE_ZOOM) }
            .forEach {
                assertEquals(
                    centerRowY,
                    it.top,
                    "$it scheduled for download does not start on center row"
                )
                assertEquals(
                    centerRowY,
                    it.bottom,
                    "$it scheduled for download does not end on center row"
                )
            }

        // setting the minDownloadDistance at half tile width should download three rows of tiles
        val bigDownloadDistance = 250.0
        val bigData = importGpx(
            inputGpx.byteInputStream(),
            displayTrack = true,
            findDownloadBBoxes = true,
            bigDownloadDistance
        ).getOrThrow()
        assertInvariants(originalTrackPoints, bigData, bigDownloadDistance)
        bigData.downloadBBoxes
            .map { it.enclosingTilesRect(ApplicationConstants.DOWNLOAD_TILE_ZOOM) }
            .forEach {
                assertEquals(
                    centerRowY - 1,
                    it.top,
                    "$it scheduled for download does not start one below center row"
                )
                assertEquals(
                    centerRowY + 1,
                    it.bottom,
                    "$it scheduled for download does not start one above center row"
                )
            }
    }

    @Test
    fun `north-south line downloads necessary area`() = runBlocking {
        // at zoom level 16, one tile is 0.005° longitude wide, or ~250m at 60° latitude
        // LatLon(.., 0.0025) is in the middle of the column  to the east of the 0 meridian
        require(ApplicationConstants.DOWNLOAD_TILE_ZOOM == 16)
        val originalTrackPoints = arrayListOf(
            TrackPoint("59.9", "0.0025"),
            TrackPoint("60.1", "0.0025"),
        )
        val inputGpx = minimalGpxBuilder(originalTrackPoints)
        val centerTileX =
            originalTrackPoints[0].toLatLon()
                .enclosingTilePos(ApplicationConstants.DOWNLOAD_TILE_ZOOM).x

        // setting the minDownloadDistance well below half tile width should download only one column
        val smallMinDownloadDistance = 50.0
        val smallData = importGpx(
            inputGpx.byteInputStream(),
            displayTrack = true,
            findDownloadBBoxes = true,
            smallMinDownloadDistance
        ).getOrThrow()
        assertInvariants(originalTrackPoints, smallData, smallMinDownloadDistance)
        smallData.downloadBBoxes
            .map { it.enclosingTilesRect(ApplicationConstants.DOWNLOAD_TILE_ZOOM) }
            .forEach {
                assertEquals(
                    centerTileX,
                    it.left,
                    "$it scheduled for download does not start on center column"
                )
                assertEquals(
                    centerTileX,
                    it.right,
                    "$it scheduled for download does not end on center column"
                )
            }

        // setting the minDownloadDistance at 1.5 times tile width should download five columns
        val bigDownloadDistance = 375.0
        val bigData = importGpx(
            inputGpx.byteInputStream(),
            displayTrack = true,
            findDownloadBBoxes = true,
            bigDownloadDistance
        ).getOrThrow()
        assertInvariants(originalTrackPoints, bigData, bigDownloadDistance)
        bigData.downloadBBoxes
            .map { it.enclosingTilesRect(ApplicationConstants.DOWNLOAD_TILE_ZOOM) }
            .forEach {
                assertEquals(
                    centerTileX - 2,
                    it.left,
                    "$it scheduled for download does not start two left of center column"
                )
                assertEquals(
                    centerTileX + 2,
                    it.right,
                    "$it scheduled for download does not end two right of center column"
                )
            }
    }

    @Test
    fun `line close to corner downloads adjacent tile`() = runBlocking {
        /*
        a line barely not touching the corner of a tile should download all four tiles around
        the corner - even if one of them is not touched by the line itself

        this test takes the four tiles around the origin LatLon(0.0, 0.0), named NW, NE, SE, SW
        in clockwise direction

        a diagonal line from south west to north east just below the origin would cross the tiles
        NE, SE and SW, but not NW

        if minDownloadDistance is greater than the distance of the line to the origin, NW should
        still be downloaded; if not by some margin, it should be omitted
        */

        // at zoom level 16, one tile is 0.005° wide / high, or ~500m wide / high on equator
        require(ApplicationConstants.DOWNLOAD_TILE_ZOOM == 16)
        val lineOriginDistance = 100.0
        val startPoint = LatLon(-0.002, -0.002).translate(lineOriginDistance, 135.0)
        val endPoint = LatLon(0.002, 0.002).translate(lineOriginDistance, 135.0)
        val nWCenterPoint = LatLon(0.0025, -0.0025)

        val originalTrackPoints = arrayListOf(
            startPoint.toTrackPoint(),
            endPoint.toTrackPoint(),
        )
        val inputGpx = minimalGpxBuilder(originalTrackPoints)

        // area around line should touch NW
        val touchingMinDownloadDistance = lineOriginDistance + 0.001
        val touchingData = importGpx(
            inputGpx.byteInputStream(),
            displayTrack = true,
            findDownloadBBoxes = true,
            touchingMinDownloadDistance
        ).getOrThrow()
        assertInvariants(originalTrackPoints, touchingData, touchingMinDownloadDistance)
        assertTrue(
            touchingData.downloadBBoxes.any { it.contains(nWCenterPoint) },
            "north west center point not contained"
        )

        // area around line should not touch NW
        val noTouchMinDownloadDistance = lineOriginDistance / 2
        val noTouchData = importGpx(
            inputGpx.byteInputStream(),
            displayTrack = true,
            findDownloadBBoxes = true,
            noTouchMinDownloadDistance
        ).getOrThrow()
        assertInvariants(originalTrackPoints, noTouchData, noTouchMinDownloadDistance)
        assertTrue(
            !noTouchData.downloadBBoxes.any { it.contains(nWCenterPoint) },
            "north west center point contained even if it should not"
        )
    }

    @Test
    fun `works around equator`() = runBlocking {
        val originalTrackPoints = arrayListOf(
            TrackPoint("0.0", "73.1"),
            TrackPoint("-0.3", "74.2"),
        )
        val minDownloadDistance = 100.0

        val inputGpx = minimalGpxBuilder(originalTrackPoints)
        val data = importGpx(
            inputGpx.byteInputStream(),
            displayTrack = true,
            findDownloadBBoxes = true,
            minDownloadDistance
        )
        assertInvariants(originalTrackPoints, data.getOrThrow(), minDownloadDistance)
    }

    @Test
    fun `works with wrap-around longitude`() = runBlocking {
        val originalTrackPoints = arrayListOf(
            TrackPoint("-33.0", "164.9"),
            TrackPoint("-35.1", "-170.3"),
        )
        val minDownloadDistance = 100.0

        val inputGpx = minimalGpxBuilder(originalTrackPoints)
        val data = importGpx(
            inputGpx.byteInputStream(),
            displayTrack = true,
            findDownloadBBoxes = true,
            minDownloadDistance
        )
        assertInvariants(originalTrackPoints, data.getOrThrow(), minDownloadDistance)
    }

    @Test
    fun `works around north pole`() = runBlocking {
        // TODO sgr: would actually like to run test with lat: 90.0, but this fails
        // latitude should be between -85.0511 and 85.0511 to be within OSM
        // tiles apparently, see https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
        // TilesRect.lat2tile gives negative numbers for latitude > 85.08, which leads to huge
        // number of tiles => maybe LatLon.checkValidity should require latitude to be <= 85.0?
        val originalTrackPoints = arrayListOf(
            TrackPoint("83.0", "-44.1"),
            TrackPoint("84.0", "178.2"),
        )
        val minDownloadDistance = 500.0

        val inputGpx = minimalGpxBuilder(originalTrackPoints)
        val data = importGpx(
            inputGpx.byteInputStream(),
            displayTrack = false,
            findDownloadBBoxes = true,
            minDownloadDistance
        )
        assertInvariants(originalTrackPoints, data.getOrThrow(), minDownloadDistance)
    }

    private fun assertInvariants(
        originalTrackPoints: List<TrackPoint>,
        importData: GpxImportData,
        minDownloadDistance: Double,
    ) {
        originalTrackPoints.forEach { trackPoint ->
            assertTrue(
                importData.segments.any { it.contains(trackPoint.toLatLon()) },
                "originalTrackPoint $trackPoint not contained in displayed segments"
            )
            assertTrue(
                importData.downloadBBoxes.any { it.contains(trackPoint.toLatLon()) },
                "originalTrackPoint $trackPoint not contained in area to download"
            )
            for (testAngle in 0..360 step 10) {
                val testPoint =
                    trackPoint.toLatLon().translate(minDownloadDistance, testAngle.toDouble())
                assertTrue(
                    importData.downloadBBoxes.any { it.contains(testPoint) },
                    "$testPoint <= $minDownloadDistance away from $trackPoint not included in area to download"
                )
            }

        }
    }
}
