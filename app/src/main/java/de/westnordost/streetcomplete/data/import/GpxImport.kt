package de.westnordost.streetcomplete.data.import

import android.util.Log
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.TilePos
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilesRect
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.toPolygon
import de.westnordost.streetcomplete.util.math.area
import de.westnordost.streetcomplete.util.math.distanceTo
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.math.initialBearingTo
import de.westnordost.streetcomplete.util.math.translate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import kotlin.math.sqrt

private const val TAG = "GpxImport"

data class GpxImportData(
    val displayTrack: Boolean,
    val downloadAlongTrack: Boolean,
    val segments: List<List<LatLon>>,
    val downloadBBoxes: List<BoundingBox>,
    val areaToDownloadInSqkm: Double,
)

internal class DecoratedBoundingBox(
    val polygon: Iterable<LatLon>,
    requestedTiles: Set<TilePos>? = null,
) {
    val boundingBox = polygon.enclosingBoundingBox()
    val tiles = boundingBox.enclosingTilesRect(ApplicationConstants.DOWNLOAD_TILE_ZOOM)
        .asTilePosSequence()
    val numberOfTiles = tiles.count()
    val requestedTiles = when (requestedTiles) {
        null -> {
            tiles.toHashSet()
        }

        else -> {
            requestedTiles
        }
    }
}

/**
 * @param inputStream valid XML according to http://www.topografix.com/GPX/1/1 schema
 * Note: the caller is responsible to close the inputStream as appropriate
 * @param displayTrack display the track on the map after import
 * @param findDownloadBBoxes  if true, compute bounding boxes which need to be downloaded to cover the track
 * @param minDownloadDistance in meters; all points within minDownloadDistance along the track should be downloaded
 */
suspend fun importGpx(
    inputStream: InputStream,
    displayTrack: Boolean,
    findDownloadBBoxes: Boolean,
    minDownloadDistance: Double,
): Result<GpxImportData> = withContext(Dispatchers.Default) {
    require(minDownloadDistance in 10.0..500.0) {
        "minDownloadDistance needs to be of reasonable size"
    }
    val trackSegments = parseGpxFile(inputStream)

    if (findDownloadBBoxes) {
        return@withContext importWithBBoxes(trackSegments, displayTrack, minDownloadDistance)
    } else {
        return@withContext importTrackOnly(trackSegments, displayTrack)
    }
}

private fun importTrackOnly(
    trackSegments: Sequence<TrackSegment>,
    displayTrack: Boolean,
): Result<GpxImportData> {
    return Result.success(
        GpxImportData(
            displayTrack,
            false,
            trackSegments
                .map { it.toList() }
                .toList(),
            arrayListOf(),
            0.0
        )
    )
}

private fun importWithBBoxes(
    trackSegments: Sequence<TrackSegment>,
    displayTrack: Boolean,
    minDownloadDistance: Double,
): Result<GpxImportData> {
    /* Algorithm overview:
    * Given that two resampled points A and B are at most 2 * minDownloadDistance away from each
    * other and any track point between them is at most minDownloadDistance away from either A or B,
    * an area that fully contains the track between A and B is given by a square S_track centered
    * on the middle point between A and B, with side length 2 * minDownloadDistance and rotated
    * such that two of its sides align with the vector from A to B. As we need to cover the area
    * within minDownloadDistance of any track point (which might lie almost on the edge of S_track),
    * a square S_min centered and rotated the same as S_track, but with
    * side length = 4 * minDownloadDistance is a handy upper bound.
    *
    * If we download two north-south aligned squares centered on A and B, they are guaranteed to
    * contain S_min if their side length is at least 4 * minDownloadDistance / sqrt(2) - the worst
    * case being were S_min is rotated 45 degrees with respect to the aligned squares.
    */
    val maxSampleDistance = 2 * minDownloadDistance
    val coveringSquareHalfLength = 2 * minDownloadDistance / sqrt(2.0)

    // TODO sgr: find a good way of retrieving original track points and area to download.
    //  Up to this point, working on sequences seems clean to me and would be easy if we only need
    //  the bounding boxes. Retrieving additional info seems to be awkward though.
    //  -
    //  One workaround might be to parse the file twice (once only to retrieve the original track
    //  points) and omit estimating any download size.
    val originalTrackPoints = arrayListOf<ArrayList<LatLon>>()
    val mergedBBoxes = arrayListOf<BoundingBox>()
    val areaToDownloadInSqkm = trackSegments
        // TODO sgr: this seems to be an awkward way of smuggling out original track points
        .map {
            originalTrackPoints.add(arrayListOf())
            it.onEach { trackPoint ->
                originalTrackPoints.last().add(trackPoint)
            }
        }
        .map { trackSegment ->
            trackSegment
                .addInterpolatedPoints(maxSampleDistance)
                .discardRedundantPoints(maxSampleDistance)
                .mapToCenteredSquares(coveringSquareHalfLength)
                .determineBBoxes()
        }
        .flatten()
        .mergeBBoxes()
        // TODO sgr: this seems to be an awkward way of smuggling out bounding boxes
        .onEach {
            mergedBBoxes.add(it.boundingBox)
        }
        .flatMap { it.tiles }
        .distinct()
        .sumOf { it.asBoundingBox(ApplicationConstants.DOWNLOAD_TILE_ZOOM).area() } / 1000000

    return Result.success(
        GpxImportData(
            displayTrack,
            true,
            originalTrackPoints,
            mergedBBoxes,
            areaToDownloadInSqkm
        )
    )
}

/**
 * Merge bounding boxes to save download calls in trade for a few more unique tiles
 * downloaded.
 *
 * The algorithm merges adjacent boxes if the merged box still has a good enough ratio
 * of actually requested vs total number of tiles downloaded.
 */
internal fun Sequence<DecoratedBoundingBox>.mergeBBoxes(): Sequence<DecoratedBoundingBox> {
    val inputIterator = this.iterator()
    if (!inputIterator.hasNext())
        return emptySequence()
    return sequence {
        var mergedBBox = inputIterator.next()
        while(inputIterator.hasNext()) {
            val bBox = inputIterator.next()
            val candidateBBox = DecoratedBoundingBox(
                mergedBBox.polygon + bBox.polygon,
                mergedBBox.requestedTiles.plus(bBox.tiles)
            )
            val requestedRatio =
                candidateBBox.requestedTiles.size.toDouble() / candidateBBox.numberOfTiles
            Log.d(TAG, "requestedRatio = $requestedRatio)")
            // requestedRatio >= 0.75 is a good compromise, as this allows downloading three
            // neighbouring tiles at zoom level x in a single call at level x-1
            mergedBBox = if (requestedRatio >= 0.75) {
                candidateBBox
            } else {
                yield(mergedBBox)
                bBox
            }

        }
        yield(mergedBBox)
    }
}

/**
 * Reduce a sequence of bounding boxes by
 * - dropping boxes which don't contribute additional tiles to download
 * - merging adjacent boxes if no additional tiles are contained in the merged box
 *
 * the mapped boxes are also decorated with some cached data for future processing.
 */
internal fun Sequence<BoundingBox>.determineBBoxes(): Sequence<DecoratedBoundingBox> {
    val inputIterator = this.map { DecoratedBoundingBox(it.toPolygon()) }.withIndex().iterator()
    if (!inputIterator.hasNext()) {
        return emptySequence()
    }

    val uniqueTilesToDownload = HashSet<TilePos>()
    return sequence {
        var currentBBox = inputIterator.next().value
        uniqueTilesToDownload.addAll(currentBBox.tiles)

        for ((index, newBBox) in inputIterator) {
            if (newBBox.tiles.all { it in uniqueTilesToDownload }) {
                Log.d(TAG, "omit bounding box #$index, all tiles already scheduled for download")
                continue
            }

            val extendedBBox = DecoratedBoundingBox(currentBBox.polygon + newBBox.polygon)
            currentBBox = if (
                extendedBBox.numberOfTiles <= (currentBBox.tiles + newBBox.tiles).toHashSet().size
            ) {
                // no additional tile needed to extend the polygon and download newBBox together with currentBBox
                Log.d(TAG, "extend currentBBox with bounding box #$index")
                extendedBBox
            } else {
                Log.d(TAG, "retain currentBBox, start new with bounding box #$index")
                yield(currentBBox)
                uniqueTilesToDownload.addAll(currentBBox.tiles)
                newBBox
            }
        }
        yield(currentBBox)
    }
}

/**
 * Transform a sequence of points to a sequence of north-south aligned bounding boxes centered on
 * these points.
 *
 * @param halfSideLength > 0.0, in meters
 */
internal fun Sequence<LatLon>.mapToCenteredSquares(halfSideLength: Double): Sequence<BoundingBox> {
    require(halfSideLength > 0.0) {
        "halfSideLength has to be positive"
    }
    return map {
        arrayListOf(
            it.translate(halfSideLength, 0.0),
            it.translate(halfSideLength, 90.0),
            it.translate(halfSideLength, 180.0),
            it.translate(halfSideLength, 270.0)
        ).enclosingBoundingBox()
    }
}

/**
 * Ensure points are at most samplingDistance away from each other.
 *
 * Given two consecutive points A, B which are more than samplingDistance away from each other,
 * add intermediate points on the line from A to B, samplingDistance away from each other until the
 * last one is <= samplingDistance away from B.
 * A and B are always retained, even if they are < samplingDistance away from each other.
 *
 * @param samplingDistance > 0.0, in meters
 */
internal fun Sequence<LatLon>.addInterpolatedPoints(samplingDistance: Double): Sequence<LatLon> {
    require(samplingDistance > 0.0) {
        "samplingDistance has to be positive"
    }

    val inputIterator = this.iterator()
    if (!inputIterator.hasNext()) {
        return emptySequence()
    }
    var lastPoint = inputIterator.next()
    return sequence {
        while (inputIterator.hasNext()) {
            val currentPoint = inputIterator.next()
            this.yieldAll(interpolate(lastPoint, currentPoint, samplingDistance))
            lastPoint = currentPoint
        }
        yield(lastPoint)
    }
}

/**
 * Interpolate points between start (included) and end (not included)
 *
 * Returned points are samplingDistance away from each other and on the line between start and end.
 * The last returned point is <= samplingDistance away from end.
 *
 * @param samplingDistance > 0.0, in meters
 */
private fun interpolate(start: LatLon, end: LatLon, samplingDistance: Double): Sequence<LatLon> =
    sequence {
        require(samplingDistance > 0.0) {
            "samplingDistance has to be positive"
        }

        var intermediatePoint = start
        while (true) {
            yield(intermediatePoint)
            if (intermediatePoint.distanceTo(end) <= samplingDistance) {
                break
            }
            intermediatePoint = intermediatePoint.translate(
                samplingDistance,
                intermediatePoint.initialBearingTo(end)
            )
        }
    }

/**
 * Discard redundant points, such that no three adjacent points A, B, C remain where B is less than
 * samplingDistance away from both A and C
 *
 * @param samplingDistance > 0.0, in meters
 */
internal fun Sequence<LatLon>.discardRedundantPoints(samplingDistance: Double): Sequence<LatLon> {
    require(samplingDistance > 0.0) {
        "samplingDistance has to be positive"
    }

    val inputIterator = this.iterator()
    if (!inputIterator.hasNext()) {
        return emptySequence()
    }
    return sequence {
        var lastRetainedPoint = inputIterator.next()
        yield(lastRetainedPoint)

        if (inputIterator.hasNext()) {
            var candidatePoint = inputIterator.next()
            while (inputIterator.hasNext()) {
                val currentPoint = inputIterator.next()
                if (lastRetainedPoint.distanceTo(candidatePoint) < samplingDistance
                    && candidatePoint.distanceTo(currentPoint) < samplingDistance
                ) {
                    // discard candidatePoint
                } else {
                    lastRetainedPoint = candidatePoint
                    yield(lastRetainedPoint)
                }
                candidatePoint = currentPoint
            }
            yield(candidatePoint)
        }
    }
}
