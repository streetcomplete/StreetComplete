package de.westnordost.streetcomplete.util.location

import android.location.Location
import de.westnordost.streetcomplete.util.ktx.elapsedDuration
import kotlin.time.Duration.Companion.minutes

// Based on https://web.archive.org/web/20180424190538/https://developer.android.com/guide/topics/location/strategies.html#BestEstimate

/** Determines whether this Location reading is better than the previous Location fix */
fun Location.isBetterThan(previous: Location?): Boolean {
    // Check whether this is a valid location at all.
    // Happened once that lat/lon is NaN, maybe issue of that particular device
    if (longitude.isNaN() || latitude.isNaN()) return false

    // A new location is always better than no location
    if (previous == null) return true

    // Check whether the new location fix is newer or older
    // we use elapsedRealtimeNanos instead of epoch time because some devices have issues
    // that may lead to incorrect GPS location.time (e.g. GPS week rollover, but also others)
    val locationTimeDiff = elapsedDuration - previous.elapsedDuration
    val isMuchNewer = locationTimeDiff > 2.minutes
    val isMuchOlder = locationTimeDiff < (-2).minutes
    val isNewer = locationTimeDiff.isPositive()

    // Check whether the new location fix is more or less accurate
    val accuracyDelta = accuracy - previous.accuracy
    val isLessAccurate = accuracyDelta > 0f
    val isMoreAccurate = accuracyDelta < 0f
    val isMuchLessAccurate = accuracyDelta > 200f

    val isFromSameProvider = provider == previous.provider

    // Determine location quality using a combination of timeliness and accuracy
    return when {
        // the user has likely moved
        isMuchNewer -> true
        // If the new location is more than two minutes older, it must be worse
        isMuchOlder -> false
        isMoreAccurate -> true
        isNewer && !isLessAccurate -> true
        isNewer && !isMuchLessAccurate && isFromSameProvider -> true
        else -> false
    }
}
