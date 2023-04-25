package de.westnordost.streetcomplete.screens.main

import android.location.Location
import de.westnordost.streetcomplete.util.math.EARTH_RADIUS
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos

class RecentLocationStore {
    private val recentLocations: MutableList<Location> = mutableListOf()

    /** returns a sequence of recent locations, with some minimum time and distance from each other */
    fun get(): Sequence<Location> = synchronized(recentLocations) {
        var previousLocation: Location? = null
        recentLocations.asReversed().asSequence().filter {
            val loc = previousLocation
            if (loc == null) {
                previousLocation = it
                return@filter true
            }
            if (abs(it.elapsedRealtimeNanos - loc.elapsedRealtimeNanos) > LOCATION_MIN_TIME_DIFFERENCE_NANOS
                && !loc.isTooCloseTo(it)
            ) {
                previousLocation = it
                true
            } else false
        }
    }

    fun add(location: Location) = synchronized(recentLocations) {
        recentLocations.removeAll {
            it.elapsedRealtimeNanos <= location.elapsedRealtimeNanos - LOCATION_STORE_TIME_NANOS
        }
        recentLocations.add(location)
    }
}

// ~ 5 times faster than using SphericalEarthMath distanceTo, could be 20 times when using approximate cos
private fun Location.isTooCloseTo(location: Location): Boolean {
    // see https://en.wikipedia.org/wiki/Geographical_distance#Spherical_Earth_projected_to_a_plane
    val dLat = (latitude - location.latitude) * PI / 180.0
    val dLon = (longitude - location.longitude) * PI / 180.0
    val cosDLon = cos((latitude + location.latitude) * PI / 180.0 / 2) * dLon // actually cos could be approximated using 1 - x^2 / 2 + x^4 / 24 - x^6 / 720: 4 times faster and good enough
    val distanceSquared = EARTH_RADIUS * EARTH_RADIUS * (dLat * dLat + cosDLon * cosDLon) // no need for sqrt
    return distanceSquared < MAX_DISTANCE_TO_ELEMENT_FOR_SURVEY * MAX_DISTANCE_TO_ELEMENT_FOR_SURVEY / 4
}

private const val LOCATION_STORE_TIME_NANOS = 600 * 1000 * 1000 * 1000L // 10 min
private const val LOCATION_MIN_TIME_DIFFERENCE_NANOS = 5 * 1000 * 1000 * 1000L // 5 sec
