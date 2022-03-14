package de.westnordost.streetcomplete.screens.main.map

import android.location.Location

/** Utility functions to estimate current bearing from a track. This is necessary because
 *  Location.bearingAccuracy doesn't exist on Android versions below Android API 26
 *  (Build.VERSION_CODES.O) , otherwise a solution based on this would be less code. E.g. take
 *  bearing if accuracy < X */

fun getTrackBearing(track: List<Location>): Float? {
    val last = track.lastOrNull() ?: return null
    val point = track.findLast { it.distanceTo(last) > MIN_TRACK_DISTANCE_FOR_BEARING } ?: return null
    return point.bearingTo(last)
}

private const val MIN_TRACK_DISTANCE_FOR_BEARING = 15f // 15 meters
