package de.westnordost.streetcomplete.data.import

import android.util.Log
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.TilePos
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilesRect
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.toPolygon
import de.westnordost.streetcomplete.util.math.area
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.InputStream

/**
 * Read GPX tracks and provide the data needed to
 * - display the track on the map
 * - download map data along the track for offline-use
 */
class GpxImporter {

    data class GpxImportData(
        val downloadAlongTrack: Boolean,
        val trackpoints: List<LatLon>,
        val downloadBBoxes: List<BoundingBox>,
        val areaToDownloadInSqkm: Double,
    )

    /**
     * @param[inputStream] GPX track to parse
     * @param[minDownloadDistance] in meters; points within minDownloadDistance along the track should be downloaded
     * @param[findDownloadBBoxes]: if true, compute bounding boxes which need to be downloaded to cover the track
     * @param[progressCallback]: a callback to set progress between 0 .. 100
     */
    suspend fun processGpxFile(
        inputStream: InputStream,
        minDownloadDistance: Double,
        findDownloadBBoxes: Boolean,
        progressCallback: suspend (progress: Int) -> Unit,
    ): Result<GpxImportData> {
        progressCallback(0)
        val trackSampler = parseTrack(inputStream, minDownloadDistance, findDownloadBBoxes)
            .getOrElse { return Result.failure(it) }
        progressCallback(10)

        if (!findDownloadBBoxes || trackSampler.getCoveringBoundingBoxes().isEmpty()) {
            progressCallback(100)
            return Result.success(
                GpxImportData(false, trackSampler.getOriginalPoints(), ArrayList(), 0.0)
            )
        }

        val bBoxesToDownload = determineBBoxesToDownload(trackSampler.getCoveringBoundingBoxes())
        // about 80% of computation is usually done here; scale progress from 10 - 90&
        { p -> progressCallback(10 + (p / 100.0 * 80).toInt()) }
            .getOrElse { return Result.failure(it) }
        progressCallback(90)

        val mergedBBoxes = mergeBBoxes(bBoxesToDownload)
            .getOrElse { return Result.failure(it) }
        progressCallback(95)

        val areaToDownloadInSqkm = computeAreaToDownload(mergedBBoxes)
            .getOrElse { return Result.failure(it) }
        progressCallback(100)

        return Result.success(
            GpxImportData(
                true,
                trackSampler.getOriginalPoints(),
                mergedBBoxes.map { it.boundingBox },
                areaToDownloadInSqkm
            )
        )
    }

    private suspend fun parseTrack(
        inputStream: InputStream,
        minDownloadDistance: Double,
        findDownloadBoundingBoxes: Boolean,
    ): Result<GpxTrackSampler> = withContext(Dispatchers.Default) {
        val trackSampler = GpxTrackSampler(this, minDownloadDistance, findDownloadBoundingBoxes)
        trackSampler.parse(inputStream)
        if (!isActive) {
            return@withContext Result.failure(CancellationException())
        }
        return@withContext Result.success(trackSampler)
    }

    /**
     * Reduce a list of bounding boxes covering the imported GPX track:
     * - discard boxes that are already contained in the download tiles of previously visited ones
     * - collapse neighbouring bounding boxes if the collapsed area is not larger than downloading
     *   them individually
     */
    private suspend fun determineBBoxesToDownload(
        coveringBoundingBoxes: List<BoundingBox>,
        progressCallback: suspend (progress: Int) -> Unit,
    ): Result<List<DecoratedBoundingBox>> = withContext(Dispatchers.Default) {
        var currentBBox: DecoratedBoundingBox? = null
        val bBoxesToDownload = ArrayList<DecoratedBoundingBox>()
        val uniqueTilesToDownload = HashSet<TilePos>()
        for ((index, newBBox) in coveringBoundingBoxes.map { DecoratedBoundingBox(it.toPolygon()) }
            .withIndex()) {
            if (!isActive) {
                return@withContext Result.failure(CancellationException())
            }
            if (index % (coveringBoundingBoxes.size / 20) == 0) {
                Log.d(TAG, "index = $index")
                progressCallback((index * 100) / coveringBoundingBoxes.size)
            }

            if (currentBBox == null) {
                currentBBox = newBBox
                continue
            }

            if (!newBBox.tiles.any { tilePos -> tilePos !in uniqueTilesToDownload }) {
                Log.d(TAG, "omit bounding box #$index, all tiles already scheduled for download")
                continue
            }

            val extendedBBox = DecoratedBoundingBox(currentBBox.polygon + newBBox.polygon)
            if (
            // no additional tile needed to extend the polygon and download newSquare with the same call
                extendedBBox.numberOfTiles <= (currentBBox.tiles + newBBox.tiles).toHashSet().size
                ||
                // area is not increased by extending the current polygon instead of downloading separately
                extendedBBox.area < currentBBox.area + newBBox.area
            ) {
                Log.d(TAG, "extend currentBBox with bounding box #$index")
                currentBBox = extendedBBox
            } else {
                Log.d(TAG, "schedule currentBBox, start new with bounding box #$index")
                bBoxesToDownload.add(currentBBox)
                uniqueTilesToDownload.addAll(currentBBox.tiles)

                currentBBox = newBBox
            }
        }
        currentBBox?.let { bBoxesToDownload.add(it) }
        return@withContext Result.success(bBoxesToDownload)
    }

    /**
     * Iteratively merge bounding boxes to save download calls in trade for a few more unique tiles
     * downloaded
     */
    private suspend fun mergeBBoxes(
        originalBBoxes: List<DecoratedBoundingBox>,
    ): Result<List<DecoratedBoundingBox>> = withContext(Dispatchers.Default) {
        var bBoxes = originalBBoxes.toList()
        val mergedBBoxes = ArrayList<DecoratedBoundingBox>()
        while (mergedBBoxes.size < bBoxes.size) {
            Log.d(TAG, "start a new round of bounding box merging")
            var currentBBox: DecoratedBoundingBox? = null
            for (bBox in bBoxes) {
                if (!isActive) {
                    return@withContext Result.failure(CancellationException())
                }

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
        return@withContext Result.success(mergedBBoxes)
    }

    /**
     * Compute effective downloaded area - each unique tile is downloaded only once, as the
     * downloader uses a cache
     */
    private suspend fun computeAreaToDownload(
        bBoxesToDownload: List<DecoratedBoundingBox>,
    ): Result<Double> = withContext(Dispatchers.Default) {
        val uniqueTilesToDownload = HashSet<TilePos>()
        for (bBox in bBoxesToDownload) {
            if (!isActive) {
                return@withContext Result.failure(CancellationException())
            }
            uniqueTilesToDownload.addAll(bBox.tiles)
        }
        return@withContext Result.success(
            uniqueTilesToDownload.sumOf {
                it.asBoundingBox(ApplicationConstants.DOWNLOAD_TILE_ZOOM).area()
            } / 1000000
        )
    }

    private class DecoratedBoundingBox(val polygon: Iterable<LatLon>) {
        val boundingBox = polygon.enclosingBoundingBox()
        val area = boundingBox.area()
        val tiles = boundingBox.enclosingTilesRect(ApplicationConstants.DOWNLOAD_TILE_ZOOM)
            .asTilePosSequence()
        val numberOfTiles = tiles.count()
    }

    companion object {
        private const val TAG = "GpxImporter"
    }
}
