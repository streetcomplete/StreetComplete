package de.westnordost.streetcomplete.map

import android.location.Location
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/** Utility functions to estimate current bearing from a track. This is necessary because
 *  Location.bearingAccuracy doesn't exist on Android versions below Android API 26, otherwise
 *  a solution based on this would be less code. E.g. take bearing if accuracy < X */

/** Return estimated bearing of a given track. The final result is a smoothing of the last few
 *  bearings in the track. The bearing of the last point is taken into account the most etc. */
fun getTrackBearing(track: List<Location>): Double? {
    val angles = getRelevantBearings(track)
    if (angles.isEmpty()) return null
    var x = 0.0
    var y = 0.0
    var f = 1.0
    for (angle in angles) {
        x += cos(angle * PI / 180.0) * f
        y += sin(angle * PI / 180.0) * f
        f *= BEARING_SMOOTHING
    }
    return atan2(y, x) * 180.0 / PI
}

/** Return the last X bearings from the given track, i.e. those that are not too old and not too
 *  close to each other */
private fun getRelevantBearings(track: List<Location>): List<Float> {
    var current = track.lastOrNull() ?: return emptyList()
    val result = ArrayList<Float>()
    for(i in track.lastIndex downTo 0) {
        val pos = track[i]
        // ignore if too close to last position
        if (pos.distanceTo(current) < MIN_TRACK_DISTANCE_FOR_BEARING) continue

        result.add(pos.bearingTo(current))

        current = pos

        // break when found enough
        if (result.size > MAX_TRACK_POINTS_FOR_BEARING) break
    }
    return result
}

private const val MIN_TRACK_DISTANCE_FOR_BEARING = 15f // 15 meters
private const val MAX_TRACK_POINTS_FOR_BEARING = 6
private const val BEARING_SMOOTHING = 0.5 // (0..1) the lower the value, the less smoothing
