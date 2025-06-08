package de.westnordost.streetcomplete.data.location

import de.westnordost.streetcomplete.util.math.flatDistanceTo
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlin.time.Duration

/** A store of a list of recent locations. When adding new locations, locations older than [maxAge]
 *  are cleared and it is made sure that all locations are at least [minDistanceMeters] apart but
 *  the most recent location is always at the front. */
class RecentLocations(
    private val maxAge: Duration,
    private val minDistanceMeters: Double,
) {
    private val lock = ReentrantLock()
    private val locations = ArrayDeque<Location>()

    /** returns a sequence of recent locations, most recent locations first */
    fun getAll(): Sequence<Location> = lock.withLock {
        locations.asSequence()
    }

    fun add(location: Location) = lock.withLock {
        // only add newer locations
        val firstDuration = locations.firstOrNull()?.elapsedDuration ?: Duration.ZERO
        if (firstDuration >= location.elapsedDuration) return@withLock

        // clear from deque all older than `maxAge` before inserting new
        while (
            locations.isNotEmpty() &&
            locations.last().elapsedDuration <= location.elapsedDuration - maxAge
        ) {
            locations.removeLast()
        }
        // clear from deque all closer than `minDistanceMeters` to `location` OR newer than
        // `minTimeDifference`
        while (
            locations.size > 1 &&
            locations.first().position.flatDistanceTo(location.position) < minDistanceMeters
        ) {
            locations.removeFirst()
        }

        locations.addFirst(location)
    }
}
