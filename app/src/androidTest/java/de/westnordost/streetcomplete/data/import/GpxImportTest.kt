package de.westnordost.streetcomplete.data.import

import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilePos
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilesRect
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.math.contains
import de.westnordost.streetcomplete.util.math.translate
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GpxImportTest {
    @Test
    fun downloadWorksOnSingleSegmentTrack() = runBlocking {
        val originalTrackPoints = arrayListOf(
            TrackPoint("0.0", "0.0"),
            TrackPoint("1.3", "-0.3"),
            TrackPoint("2", "-2"),
            TrackPoint("2.4", "-2.2"),
            TrackPoint("2.4", "-2.2"),
            TrackPoint("2.6", "-3"),
        )
        val inputGpx = minimalGpxBuilder(originalTrackPoints)
        val minDownloadDistance = 100.0
        val (segments, downloadBBoxes) = import(minDownloadDistance, inputGpx)
        assertInvariants(
            originalTrackPoints,
            segments,
            downloadBBoxes,
            minDownloadDistance
        )
    }

    @Test
    fun displayOnlyImportWorksOnSingleSegmentTrack() = runBlocking {
        val originalTrackPoints = arrayListOf(
            TrackPoint("-36.1", "-143.0"),
            TrackPoint("-40.2", "-179.999"),
            TrackPoint("-42.0", "179"),
            TrackPoint("-38.38", "171"),
        )

        val inputGpx = minimalGpxBuilder(originalTrackPoints)
        val (segments, _) = import(250.0, inputGpx)

        originalTrackPoints.forEach { trackPoint ->
            assertTrue(
                segments.any { it.contains(trackPoint.toLatLon()) },
                "originalTrackPoint $trackPoint not contained in displayed segments"
            )
        }
    }

    @Test
    fun eastWestLineDownloadsNecessaryArea() = runBlocking {
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
        val (smallSegments, smallDownloadBBoxes) = import(smallMinDownloadDistance, inputGpx)
        assertInvariants(
            originalTrackPoints,
            smallSegments,
            smallDownloadBBoxes,
            smallMinDownloadDistance
        )
        smallDownloadBBoxes
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
        val bigMinDownloadDistance = 250.0
        val (bigSegments, bigDownloadBBoxes) = import(bigMinDownloadDistance, inputGpx)
        assertInvariants(
            originalTrackPoints,
            bigSegments,
            bigDownloadBBoxes,
            bigMinDownloadDistance
        )
        bigDownloadBBoxes
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
    fun northSouthLineDownloadsNecessaryArea() = runBlocking {
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
        val (smallSegments, smallDownloadBBoxes) = import(smallMinDownloadDistance, inputGpx)
        assertInvariants(
            originalTrackPoints,
            smallSegments,
            smallDownloadBBoxes,
            smallMinDownloadDistance
        )
        smallDownloadBBoxes
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
        val bigMinDownloadDistance = 375.0
        val (bigSegments, bigDownloadBBoxes) = import(bigMinDownloadDistance, inputGpx)
        assertInvariants(
            originalTrackPoints,
            bigSegments,
            bigDownloadBBoxes,
            bigMinDownloadDistance
        )
        bigDownloadBBoxes
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
    fun lineCloseToCornerDownloadsAdjacentTile() = runBlocking {
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
        val (touchingSegments, touchingDownloadBBoxes) = import(
            touchingMinDownloadDistance,
            inputGpx
        )
        assertInvariants(
            originalTrackPoints,
            touchingSegments,
            touchingDownloadBBoxes,
            touchingMinDownloadDistance
        )
        assertTrue(
            touchingDownloadBBoxes.any { it.contains(nWCenterPoint) },
            "north west center point not contained"
        )

        // area around line should not touch NW
        val noTouchMinDownloadDistance = lineOriginDistance / 2
        val (noTouchSegments, noTouchDownloadBBoxes) = import(noTouchMinDownloadDistance, inputGpx)
        assertInvariants(
            originalTrackPoints,
            noTouchSegments,
            noTouchDownloadBBoxes,
            noTouchMinDownloadDistance
        )
        assertTrue(
            !noTouchDownloadBBoxes.any { it.contains(nWCenterPoint) },
            "north west center point contained even if it should not"
        )
    }

    @Test
    fun worksAroundEquator() = runBlocking {
        val originalTrackPoints = arrayListOf(
            TrackPoint("0.0", "73.1"),
            TrackPoint("-0.3", "74.2"),
        )
        val minDownloadDistance = 100.0

        val inputGpx = minimalGpxBuilder(originalTrackPoints)
        val (segments, downloadBBoxes) = import(minDownloadDistance, inputGpx)
        assertInvariants(
            originalTrackPoints,
            segments,
            downloadBBoxes,
            minDownloadDistance
        )
    }

    @Test
    fun worksWithWrapAroundLongitude() = runBlocking {
        val originalTrackPoints = arrayListOf(
            TrackPoint("-33.0", "164.9"),
            TrackPoint("-35.1", "-170.3"),
        )
        val minDownloadDistance = 100.0

        val inputGpx = minimalGpxBuilder(originalTrackPoints)
        val (segments, downloadBBoxes) = import(minDownloadDistance, inputGpx)
        assertInvariants(
            originalTrackPoints,
            segments,
            downloadBBoxes,
            minDownloadDistance
        )
    }

    @Test
    fun worksAroundNorthPole() = runBlocking {
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
        val (segments, downloadBBoxes) = import(minDownloadDistance, inputGpx)
        assertInvariants(
            originalTrackPoints,
            segments,
            downloadBBoxes,
            minDownloadDistance
        )
    }

    private suspend fun import(
        minDownloadDistance: Double,
        inputGpx: String,
    ): Pair<ArrayList<List<LatLon>>, List<BoundingBox>> {
        return coroutineScope {
            val importer = GpxImporter(minDownloadDistance)
            val segments = async {
                val segments = arrayListOf<List<LatLon>>()
                importer.segments.collect { segments.add(it.toList()) }
                segments
            }
            val bBoxes = async {
                importer.downloadBBoxes.toList()
            }
            importer.readFile(this, inputGpx.byteInputStream())
            return@coroutineScope Pair(segments.await(), bBoxes.await())
        }
    }

    private fun assertInvariants(
        originalTrackPoints: List<TrackPoint>,
        segments: List<List<LatLon>>,
        downloadBBoxes: List<BoundingBox>,
        minDownloadDistance: Double,
    ) {
        originalTrackPoints.forEach { trackPoint ->
            assertTrue(
                segments.any { it.contains(trackPoint.toLatLon()) },
                "originalTrackPoint $trackPoint not contained in displayed segments"
            )
            assertTrue(
                downloadBBoxes.any { it.contains(trackPoint.toLatLon()) },
                "originalTrackPoint $trackPoint not contained in area to download"
            )
            for (testAngle in 0..360 step 10) {
                val testPoint =
                    trackPoint.toLatLon().translate(minDownloadDistance, testAngle.toDouble())
                assertTrue(
                    downloadBBoxes.any { it.contains(testPoint) },
                    "$testPoint <= $minDownloadDistance away from $trackPoint not included in area to download"
                )
            }

        }
    }
}
