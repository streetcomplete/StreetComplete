package de.westnordost.streetcomplete.screens.main.map

import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import de.westnordost.streetcomplete.util.math.distanceTo
import de.westnordost.streetcomplete.util.math.initialBearingTo

/** Utility function to estimate current bearing from a track */
fun getTrackBearing(track: List<Trackpoint>): Double? {
    val last = track.lastOrNull()?.position ?: return null
    val point = track.findLast { it.position.distanceTo(last) > MIN_TRACK_DISTANCE_FOR_BEARING }?.position ?: return null
    return point.initialBearingTo(last)
}

private const val MIN_TRACK_DISTANCE_FOR_BEARING = 15f // 15 meters
