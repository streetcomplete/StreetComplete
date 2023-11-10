package de.westnordost.streetcomplete.data.import

import android.util.Log
import de.westnordost.osmapi.common.Handler
import de.westnordost.osmapi.common.errors.XmlParserException
import de.westnordost.osmapi.traces.GpsTrackpoint
import de.westnordost.osmapi.traces.GpxTrackParser
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.math.distanceTo
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.math.initialBearingTo
import de.westnordost.streetcomplete.util.math.translate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import java.io.InputStream
import kotlin.math.sqrt

/**
 * Coroutine-enabled GpxTrackParser to collect the original trackpoints of a GPX track and resample
 * it such that all area within minDownloadDistance of the track is covered by a list of aligned,
 * uniformly sized bounding boxes.
 */
class GpxTrackSampler private constructor(
    private val sampler: Sampler,
) : GpxTrackParser(sampler) {

    private var finishedParsing = false

    override fun parse(`in`: InputStream?): Void? {
        try {
            super.parse(`in`)
            finishedParsing = true
        } catch (e: XmlParserException) {
            if (e.cause != null && e.cause is CancellationException) {
                // nothing to do, normal coroutine cancellation
            } else {
                throw e
            }
        }
        return null
    }

    /**
     * only call this method after parsing a file
     */
    fun getOriginalPoints(): List<LatLon> {
        if (!finishedParsing) {
            throw IllegalStateException("parse a file before calling this method")
        }
        return sampler.originalPoints
    }

    /**
     * only call this method after parsing a file
     */
    fun getCoveringBoundingBoxes(): List<BoundingBox> {
        if (!finishedParsing) {
            throw IllegalStateException("parse a file before calling this method")
        }
        return sampler.getCoveringSquareCenters().map {
            arrayListOf(
                it.translate(sampler.coveringSquareHalfLength, 0.0),
                it.translate(sampler.coveringSquareHalfLength, 90.0),
                it.translate(sampler.coveringSquareHalfLength, 180.0),
                it.translate(sampler.coveringSquareHalfLength, 270.0)
            ).enclosingBoundingBox()
        }
    }

    /**
     * Handler to collect the original trackpoints and resample the track.
     *
     * Algorithm overview:
     * Given that two resampled points A and B are at most 2 * minDownloadDistance away from each
     * other and any trackpoint between them is at most minDownloadDistance away from either A or B,
     * an area that fully contains the track between A and B is given by a square S_track centered
     * on the middle point between A and B, with side length 2 * minDownloadDistance and rotated
     * such that two of its sides align with the vector from A to B. As we need to cover the area
     * within minDownloadDistance of any trackpoint (which might lie almost on the edge of S_track),
     * a square S_min centered and rotated the same as S_track, but with
     * side length = 4 * minDownloadDistance is a handy upper bound.
     *
     * If we download two non-rotated squares centered on A and B, they are guaranteed to contain
     * S_min if their side length is at least 4 * minDownloadDistance / sqrt(2) - the worst case
     * being were S_min is rotated 45 degrees with respect to the non-rotated squares.
     */
    private class Sampler(
        private val coroutineScope: CoroutineScope,
        private val findCenterPoints: Boolean,
        private val minDownloadDistance: Double,
    ) : Handler<GpsTrackpoint> {

        val coveringSquareHalfLength: Double

        init {
            // values that are too small need a lot of computation on the device and many downloads
            // values that are too big lead to huge downloaded areas
            require(minDownloadDistance in 10.0..500.0) {
                "minDownloadDistance needs to be of reasonable size"
            }

            coveringSquareHalfLength = 2 * minDownloadDistance / sqrt(2.0)
        }

        var originalPoints: MutableList<LatLon> = ArrayList()
        private var centerPoints: MutableList<LatLon> = ArrayList()
        private var candidatePoint: LatLon? = null

        override fun handle(tea: GpsTrackpoint?) {
            if (!coroutineScope.isActive) {
                throw CancellationException()
            }

            tea?.position?.let {
                val trackpoint = LatLon(it.latitude, it.longitude)
                originalPoints.add(trackpoint)

                if (!findCenterPoints) {
                    // no need to do the hard work
                    return
                }

                if (candidatePoint == null) {
                    candidatePoint = trackpoint
                    return
                }

                // interpolate between current trackpoint and last candidate point to ensure
                // sampling rate is high enough
                while (trackpoint.distanceTo(candidatePoint!!) > (2 * minDownloadDistance)) {
                    if (!coroutineScope.isActive) {
                        throw CancellationException()
                    }

                    centerPoints.add(candidatePoint!!)
                    candidatePoint = candidatePoint!!.translate(
                        distance = 2 * minDownloadDistance,
                        angle = candidatePoint!!.initialBearingTo(trackpoint)
                    )
                    Log.d(TAG, "add interpolated point: $candidatePoint")
                }

                // discard redundant candidate points
                if (centerPoints.isNotEmpty() &&
                    centerPoints.last().distanceTo(candidatePoint!!) < minDownloadDistance &&
                    trackpoint.distanceTo(candidatePoint!!) < minDownloadDistance
                ) {
                    Log.d(TAG, "omit point included in previous and next area: $candidatePoint")
                    candidatePoint = trackpoint
                    return
                }

                centerPoints.add(candidatePoint!!)
                candidatePoint = trackpoint
            }
        }

        fun getCoveringSquareCenters(): List<LatLon> {
            if (!findCenterPoints) {
                throw IllegalStateException("covering squares not available, as findCenterPoints was set to false ")
            }

            // add endpoint separately, as no point after it is visited by the handler
            return centerPoints + listOfNotNull(candidatePoint)
        }
    }

    companion object {
        private const val TAG = "GpxTrackSampler"

        operator fun invoke(
            coroutineScope: CoroutineScope,
            findCoveringBoundingBoxes: Boolean,
            minDownloadDistance: Double,
        ): GpxTrackSampler {
            val sampler = Sampler(coroutineScope, findCoveringBoundingBoxes, minDownloadDistance)
            return GpxTrackSampler(sampler)
        }
    }
}
