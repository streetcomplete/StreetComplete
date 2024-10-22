package de.westnordost.streetcomplete.data.import

import android.util.Log
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.TilePos
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilesRect
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.toPolygon
import de.westnordost.streetcomplete.util.math.distanceTo
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.math.initialBearingTo
import de.westnordost.streetcomplete.util.math.translate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.launch
import java.io.InputStream
import kotlin.math.sqrt

private const val TAG = "GpxImport"

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

class GpxImporter(
    minDownloadDistance: Double,
) {
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
    private val maxSampleDistance = 2 * minDownloadDistance
    private val coveringSquareHalfLength = 2 * minDownloadDistance / sqrt(2.0)

    private val _mutableSegments = MutableSharedFlow<SharedFlow<LatLon?>?>()
    private val _sharedSegments = _mutableSegments.asSharedFlow()

    val segments = _sharedSegments.takeWhile { it != null }.filterNotNull()
        .map { flow -> flow.takeWhile { it != null }.filterNotNull() }

    @OptIn(ExperimentalCoroutinesApi::class)
    val downloadBBoxes = _sharedSegments.takeWhile { it != null }.filterNotNull()
        .map { flow ->
            flow.takeWhile { it != null }.filterNotNull()
                .addInterpolatedPoints(maxSampleDistance)
                .discardRedundantPoints(maxSampleDistance)
                .mapToCenteredSquares(coveringSquareHalfLength)
                .determineBBoxes()
        }
        .flattenConcat()
        .mergeBBoxes()
        .map { it.boundingBox }

    fun readFile(scope: CoroutineScope, inputStream: InputStream) {
        scope.launch {
            parseGpxFile(inputStream).collect { segment ->
                val m = MutableSharedFlow<LatLon?>()
                _mutableSegments.emit(m.asSharedFlow())
                segment.collect {
                    m.emit(it)
                }
                m.emit(null)
            }

            _mutableSegments.emit(null)
        }
    }
}

/**
 * Merge bounding boxes to save download calls in trade for a few more unique tiles
 * downloaded.
 *
 * The algorithm merges adjacent boxes if the merged box still has a good enough ratio
 * of actually requested vs total number of tiles downloaded.
 */
internal fun Flow<DecoratedBoundingBox>.mergeBBoxes(): Flow<DecoratedBoundingBox> {
    return flow {
        lateinit var mergedBBox: DecoratedBoundingBox
        var initialized = false
        collect {
            if (initialized) {
                val candidateBBox = DecoratedBoundingBox(
                    mergedBBox.polygon + it.polygon,
                    mergedBBox.requestedTiles.plus(it.tiles)
                )
                val requestedRatio =
                    candidateBBox.requestedTiles.size.toDouble() / candidateBBox.numberOfTiles
                Log.d(TAG, "requestedRatio = $requestedRatio")
                // requestedRatio >= 0.75 is a good compromise, as this allows downloading three
                // neighbouring tiles at zoom level x in a single call at level x-1
                mergedBBox = if (requestedRatio >= 0.75) {
                    candidateBBox
                } else {
                    emit(mergedBBox)
                    it
                }
            } else {
                mergedBBox = it
                initialized = true
            }
        }
        if (initialized) {
            emit(mergedBBox)
        }
    }
}

/**
 * Reduce a flow of bounding boxes by
 * - dropping boxes which don't contribute additional tiles to download
 * - merging adjacent boxes if no additional tiles are contained in the merged box
 *
 * the mapped boxes are also decorated with some cached data for future processing.
 */
internal fun Flow<BoundingBox>.determineBBoxes(): Flow<DecoratedBoundingBox> {
    val inputFlow = this.map { DecoratedBoundingBox(it.toPolygon()) }.withIndex()
    val uniqueTilesToDownload = HashSet<TilePos>()

    return flow {
        lateinit var currentBBox: DecoratedBoundingBox
        var initialized = false
        inputFlow.collect {
            val newBBox = it.value
            if (!initialized) {
                currentBBox = newBBox
                uniqueTilesToDownload.addAll(currentBBox.tiles)
                initialized = true
            } else if (newBBox.tiles.all { bBox -> bBox in uniqueTilesToDownload }) {
                Log.d(TAG, "omit bounding box #$it.index, all tiles already scheduled for download")
            } else {
                val extendedBBox = DecoratedBoundingBox(currentBBox.polygon + newBBox.polygon)
                currentBBox = if (
                    extendedBBox.numberOfTiles <= (currentBBox.tiles + newBBox.tiles).toHashSet().size
                ) {
                    // no additional tile needed to extend the polygon and download newBBox together with currentBBox
                    Log.d(TAG, "extend currentBBox with bounding box #$it.index")
                    extendedBBox
                } else {
                    Log.d(TAG, "retain currentBBox, start new with bounding box #$it.index")
                    emit(currentBBox)
                    uniqueTilesToDownload.addAll(currentBBox.tiles)
                    newBBox
                }
            }
        }
        if (initialized) {
            emit(currentBBox)
        }
    }
}

/**
 * Transform a flow of points to a flow of north-south aligned bounding boxes centered on
 * these points.
 *
 * @param halfSideLength > 0.0, in meters
 */
internal fun Flow<LatLon>.mapToCenteredSquares(halfSideLength: Double): Flow<BoundingBox> {
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
internal fun Flow<LatLon>.addInterpolatedPoints(samplingDistance: Double): Flow<LatLon> =
    flow {
        require(samplingDistance > 0.0) {
            "samplingDistance has to be positive"
        }

        lateinit var lastPoint: LatLon
        var initialized = false
        collect {
            if (initialized) {
                this.emitAll(interpolate(lastPoint, it, samplingDistance))
            } else {
                initialized = true
            }
            lastPoint = it
        }
        if (initialized) {
            emit(lastPoint)
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
private fun interpolate(start: LatLon, end: LatLon, samplingDistance: Double): Flow<LatLon> = flow {
    require(samplingDistance > 0.0) {
        "samplingDistance has to be positive"
    }

    var intermediatePoint = start
    while (true) {
        emit(intermediatePoint)
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
internal fun Flow<LatLon>.discardRedundantPoints(samplingDistance: Double): Flow<LatLon> = flow {
    require(samplingDistance > 0.0) {
        "samplingDistance has to be positive"
    }

    lateinit var lastRetainedPoint: LatLon
    lateinit var candidatePoint: LatLon
    var initializedLastRetainedPoint = false
    var initializedCandidatePoint = false
    collect {
        if (!initializedLastRetainedPoint) {
            lastRetainedPoint = it
            initializedLastRetainedPoint = true
            emit(it)
        } else if (!initializedCandidatePoint) {
            candidatePoint = it
            initializedCandidatePoint = true
        } else {
            val currentPoint = it
            if (lastRetainedPoint.distanceTo(candidatePoint) < samplingDistance
                && candidatePoint.distanceTo(currentPoint) < samplingDistance
            ) {
                // discard candidatePoint
            } else {
                lastRetainedPoint = candidatePoint
                emit(lastRetainedPoint)
            }
            candidatePoint = currentPoint
        }
    }
    if (initializedCandidatePoint) {
        emit(candidatePoint)
    }
}
