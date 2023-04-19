package de.westnordost.streetcomplete.screens.main

import android.location.Location
import de.westnordost.streetcomplete.util.math.EARTH_RADIUS
import kotlin.math.PI
import kotlin.math.cos

class RecentLocationStore {
    private val recentLocations: MutableList<Location> = mutableListOf()

    fun get(): Sequence<Location> = synchronized(recentLocations) {
        var previousLocation: Location? = null
        recentLocations.asReversed().asSequence().filter {
            val loc = previousLocation
            if (loc == null) {
                previousLocation = it
                return@filter true
            }
            if (!loc.isWithin10mOf(it)) {
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

private fun Location.isWithin10mOf(location: Location): Boolean {
    // see https://en.wikipedia.org/wiki/Geographical_distance#Spherical_Earth_projected_to_a_plane
    val dLat = (latitude - location.latitude) * PI / 180.0
    val dLon = longitude - location.longitude * PI / 180.0
    val cosDLon = cos((latitude + location.latitude) * PI / 180.0 / 2) * dLon // actually cos could be approximated using 1 - x^2 / 2 + x^4 / 24
    val distanceSquared = EARTH_RADIUS * EARTH_RADIUS * (dLat * dLat + cosDLon * cosDLon) // no need for sqrt
    return distanceSquared < 10 * 10
}

private const val LOCATION_STORE_TIME_NANOS = 60 * 1000 * 1000 * 1000L
