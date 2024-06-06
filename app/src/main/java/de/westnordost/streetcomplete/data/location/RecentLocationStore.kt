package de.westnordost.streetcomplete.data.location

import android.location.Location
import de.westnordost.streetcomplete.util.ktx.elapsedDuration
import de.westnordost.streetcomplete.util.ktx.toLatLon
import de.westnordost.streetcomplete.util.math.flatDistanceTo
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

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
            if ((it.elapsedDuration - loc.elapsedDuration).absoluteValue > LOCATION_MIN_TIME_DIFFERENCE
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
            && recentLocations.first().elapsedDuration <= location.elapsedDuration - LOCATION_STORE_TIME
        ) {
            recentLocations.removeFirst()
        }
        recentLocations.add(location)
    }

    companion object {
        /*
        Considerations for choosing these values:

        - users should be encouraged to *really* go right there and check even if they think they
          see it from afar already

        - just having walked by something should though still count as survey though. (It might be
          inappropriate or awkward to stop and flip out the smartphone directly there)

        - GPS position might not be updated right after they fetched it out of their pocket, but GPS
          position should be reset to "unknown" (instead of "wrong") when switching back to the app

        - the distance is the minimum distance between the quest geometry (i.e. a road) and the line
          between the user's position when he opened the quest form and the position when he pressed
          "ok", MINUS the current GPS accuracy, so it is a pretty forgiving calculation already
         */
        const val MAX_DISTANCE_TO_ELEMENT_FOR_SURVEY = 80f // m

        private val LOCATION_STORE_TIME = 10.minutes
        private val LOCATION_MIN_TIME_DIFFERENCE = 5.seconds
    }
}
