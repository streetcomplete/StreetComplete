package de.westnordost.streetcomplete.data.location

import android.location.Location
import de.westnordost.streetcomplete.util.ktx.toLatLon
import de.westnordost.streetcomplete.util.math.flatDistanceTo
import kotlin.math.abs

class RecentLocationStore {
    private val recentLocations = ArrayDeque<Location>()

    /** returns a sequence of recent locations, with some minimum time and distance from each other */
    fun get(): Sequence<Location> = synchronized(recentLocations) {
        var previousLocation: Location? = null
        recentLocations.reversed().asSequence().filter {
            val loc = previousLocation
            if (loc == null) {
                previousLocation = it
                return@filter true
            }
            if (abs(it.elapsedRealtimeNanos - loc.elapsedRealtimeNanos) > LOCATION_MIN_TIME_DIFFERENCE_NANOS
                && loc.toLatLon().flatDistanceTo(it.toLatLon()) >= MAX_DISTANCE_TO_ELEMENT_FOR_SURVEY / 2
            ) {
                previousLocation = it
                true
            } else {
                false
            }
        }
    }

    fun add(location: Location) = synchronized(recentLocations) {
        while (recentLocations.isNotEmpty()
            && recentLocations.first().elapsedRealtimeNanos <= location.elapsedRealtimeNanos - LOCATION_STORE_TIME_NANOS
        ) {
            recentLocations.removeFirst()
        }
        recentLocations.add(location)
    }
}

private const val LOCATION_STORE_TIME_NANOS = 600 * 1000 * 1000 * 1000L // 10 min
private const val LOCATION_MIN_TIME_DIFFERENCE_NANOS = 5 * 1000 * 1000 * 1000L // 5 sec
