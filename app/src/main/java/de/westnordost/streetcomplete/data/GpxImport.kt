package de.westnordost.streetcomplete.data

import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.TilePos
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilesRect
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.toPolygon
import de.westnordost.streetcomplete.util.logs.Log
import de.westnordost.streetcomplete.util.math.area
import de.westnordost.streetcomplete.util.math.distanceTo
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.math.initialBearingTo
import de.westnordost.streetcomplete.util.math.translate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.sqrt

private const val TAG = "GpxImport"

// file slightly modified from https://github.com/streetcomplete/StreetComplete/pull/5369
data class GpxImportData(
    val displayTrack: Boolean,
    val downloadAlongTrack: Boolean,
    val trackpoints: List<LatLon>,
    val downloadBBoxes: List<BoundingBox>,
    val areaToDownloadInSqkm: Double,
)

private class DecoratedBoundingBox(val polygon: Iterable<LatLon>) {
    val boundingBox = polygon.enclosingBoundingBox()
    val area = boundingBox.area()
    val tiles = boundingBox.enclosingTilesRect(ApplicationConstants.DOWNLOAD_TILE_ZOOM)
        .asTilePosSequence()
    val numberOfTiles = tiles.count()
}

// TODO sgr: refactor function signature when adapting UI
/**
 * @param originalTrackPoints points from GPX
 * @param displayTrack display the track on the map after import
 * @param minDownloadDistance in meters; points within minDownloadDistance along the track should be downloaded
 */
suspend fun importGpx(
    originalTrackPoints: List<LatLon>,
    displayTrack: Boolean,
    minDownloadDistance: Double,
): Result<GpxImportData> = withContext(Dispatchers.Default) {
    require(minDownloadDistance in 10.0..500.0) {
        "minDownloadDistance needs to be of reasonable size"
    }

    /* Algorithm overview:
 *
 * Given that two resampled points A and B are at most 2 * minDownloadDistance away from each
 * other and any track point between them is at most minDownloadDistance away from either A or B,
 * an area that fully contains the track between A and B is given by a square S_track centered
 * on the middle point between A and B, with side length 2 * minDownloadDistance and rotated
 * such that two of its sides align with the vector from A to B. As we need to cover the area
 * within minDownloadDistance of any track point (which might lie almost on the edge of S_track),
 * a square S_min centered and rotated the same as S_track, but with
 * side length = 4 * minDownloadDistance is a handy upper bound.
 *
 * If we download two non-rotated squares centered on A and B, they are guaranteed to contain
 * S_min if their side length is at least 4 * minDownloadDistance / sqrt(2) - the worst case
 * being were S_min is rotated 45 degrees with respect to the non-rotated squares.
     */
    val maxSampleDistance = 2 * minDownloadDistance
    val coveringSquareHalfLength = 2 * minDownloadDistance / sqrt(2.0)

    var progress = 0
    val mergedBBoxes = originalTrackPoints
        .asSequence()
        // TODO sgr: just a test how one could decorate sequences with a callback
        //  -> this approach would need orchestration at this level from UI code
        .map {
            progress++
            if (progress % 500 == 0) {
                Log.d(TAG, "updating progress: ${progress / originalTrackPoints.size}")
            }
            it
        }
        .addInterpolatedPoints(maxSampleDistance)
        .discardRedundantPoints(maxSampleDistance)
        .mapToCenteredSquares(coveringSquareHalfLength)
        .determineBBoxesToDownload()
        .mergeBBoxesToDownload()

    return@withContext Result.success(
        GpxImportData(
            displayTrack,
            true,
            originalTrackPoints,
            mergedBBoxes.map { it.boundingBox }.toList(),
            mergedBBoxes
                .flatMap { it.tiles }
                .distinct()
                .sumOf { it.asBoundingBox(ApplicationConstants.DOWNLOAD_TILE_ZOOM).area() }
                / 1000000
        )
    )
}

/**
 * TODO sgr: convert implementation to real sequence.. might be tricky
 * Iteratively merge bounding boxes to save download calls in trade for a few more unique tiles
 * downloaded
 */
private fun Sequence<DecoratedBoundingBox>.mergeBBoxesToDownload(): Sequence<DecoratedBoundingBox> {
    var bBoxes = this.toList()
    val mergedBBoxes = ArrayList<DecoratedBoundingBox>()
    while (mergedBBoxes.size < bBoxes.size) {
        Log.d(TAG, "start a new round of bounding box merging")
        var currentBBox: DecoratedBoundingBox? = null
        for (bBox in bBoxes) {
            if (currentBBox == null) {
                currentBBox = bBox
                continue
            }
            val mergedBBox = DecoratedBoundingBox(bBox.polygon + currentBBox.polygon)
            // merge two adjacent boxes if at most one additional tile needs to be downloaded to save one call
            currentBBox =
                if (mergedBBox.numberOfTiles <= (currentBBox.tiles + bBox.tiles).toHashSet().size + 1) {
                    Log.d(TAG, "merge currentBBox with previous one")
                    mergedBBox
                } else {
                    Log.d(TAG, "keep currentBBox separate from previous one")
                    mergedBBoxes.add(currentBBox)
                    bBox
                }
        }
        currentBBox?.let { mergedBBoxes.add(it) }
        if (mergedBBoxes.size < bBoxes.size) {
            Log.d(TAG, "reduced bounding boxes from ${bBoxes.size} to ${mergedBBoxes.size}")
            bBoxes = mergedBBoxes.toList()
            mergedBBoxes.clear()
        } else {
            Log.d(TAG, "final number of bounding boxes: ${mergedBBoxes.size}")
        }
    }
    return mergedBBoxes.asSequence()
}

private fun Sequence<BoundingBox>.determineBBoxesToDownload(): Sequence<DecoratedBoundingBox> {
    var currentBBox: DecoratedBoundingBox? = null
    val uniqueTilesToDownload = HashSet<TilePos>()
    val inputIterator = this.map { DecoratedBoundingBox(it.toPolygon()) }.withIndex().iterator()
    return sequence {
        for ((index, newBBox) in inputIterator) {
            if (currentBBox == null) {
                currentBBox = newBBox
                yield(newBBox)
                continue
            }

            if (!newBBox.tiles.any { tilePos -> tilePos !in uniqueTilesToDownload }) {
                Log.d(TAG, "omit bounding box #$index, all tiles already scheduled for download")
                continue
            }

            val extendedBBox = DecoratedBoundingBox(currentBBox!!.polygon + newBBox.polygon)
            currentBBox = if (
            // no additional tile needed to extend the polygon and download newBBox together with currentBBox
                extendedBBox.numberOfTiles <= (currentBBox!!.tiles + newBBox.tiles).toHashSet().size
                ||
                // downloaded area is not increased by extending the current polygon instead of downloading separately
                extendedBBox.area < currentBBox!!.area + newBBox.area
            ) {
                Log.d(TAG, "extend currentBBox with bounding box #$index")
                extendedBBox
            } else {
                Log.d(TAG, "schedule currentBBox, start new with bounding box #$index")
                yield(currentBBox!!)
                uniqueTilesToDownload.addAll(currentBBox!!.tiles)
                newBBox
            }
        }
        currentBBox?.let { yield(it) }
    }
}

/**
 * Transform a sequence of points to a sequence of bounding boxes centered on the points.
 */
private fun Sequence<LatLon>.mapToCenteredSquares(halfSideLength: Double): Sequence<BoundingBox> =
    map {
        arrayListOf(
            it.translate(halfSideLength, 0.0),
            it.translate(halfSideLength, 90.0),
            it.translate(halfSideLength, 180.0),
            it.translate(halfSideLength, 270.0)
        ).enclosingBoundingBox()
    }

/**
 * Ensure points are at most samplingDistance away from each other.
 *
 * Given two consecutive points A, B which are more than samplingDistance away from each other,
 * add intermediate points on the line from A to B, samplingDistance away from each other until the
 * last one is <= samplingDistance away from B.
 */
private fun Sequence<LatLon>.addInterpolatedPoints(samplingDistance: Double): Sequence<LatLon> {
    var candidatePoint: LatLon? = null
    val seq = this.flatMap { currentPoint ->
        if (candidatePoint == null) {
            candidatePoint = currentPoint
            return@flatMap emptySequence<LatLon>()
        }
        val interpolatedPoints = interpolate(candidatePoint!!, currentPoint, samplingDistance)
        candidatePoint = currentPoint
        return@flatMap interpolatedPoints
    }
    return seq + sequenceOf(candidatePoint).mapNotNull { it }
}

/**
 * Interpolate points between start (included) and end (not included)
 *
 * Returned points are samplingDistance away from each other and on the line between start and end.
 * The last returned point is <= samplingDistance away from end.
 */
private fun interpolate(start: LatLon, end: LatLon, samplingDistance: Double): Sequence<LatLon> =
    sequence {
        val bearing = start.initialBearingTo(end)
        var intermediatePoint = start
        while (true) {
            yield(intermediatePoint)
            intermediatePoint = intermediatePoint.translate(samplingDistance, bearing)
            if (intermediatePoint.distanceTo(end) <= samplingDistance) {
                break
            }
        }
    }

/**
 * Discard redundant points, such that no three remaining points A, B, C exist where B is less than
 * samplingDistance away from both A and C
 */
private fun Sequence<LatLon>.discardRedundantPoints(samplingDistance: Double): Sequence<LatLon> {
    var lastRetainedPoint: LatLon? = null
    var candidatePoint: LatLon? = null
    return this.flatMap { currentPoint ->
        sequence {
            if (candidatePoint == null) {
                candidatePoint = currentPoint
            } else if (lastRetainedPoint == null) {
                lastRetainedPoint = candidatePoint
                candidatePoint = currentPoint
            } else if (lastRetainedPoint!!.distanceTo(candidatePoint!!) < samplingDistance
                && candidatePoint!!.distanceTo(currentPoint) < samplingDistance
            ) {
                // discard candidatePoint
                candidatePoint = currentPoint
            } else {
                lastRetainedPoint = candidatePoint
                yield(lastRetainedPoint!!)
                candidatePoint = currentPoint
            }
        }
    } + sequenceOf(candidatePoint).mapNotNull { it }
}
