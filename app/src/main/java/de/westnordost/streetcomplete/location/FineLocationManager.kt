package de.westnordost.streetcomplete.location

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.location.Location
import android.location.LocationManager
import android.location.LocationManager.GPS_PROVIDER
import android.location.LocationManager.NETWORK_PROVIDER
import android.os.Looper
import androidx.annotation.RequiresPermission

class FineLocationManager(private val mgr: LocationManager, private var locationUpdateCallback: ((Location) -> Unit)) {

    private var lastLocation: Location? = null

    private val deviceHasGPS: Boolean get() = mgr.allProviders.contains(GPS_PROVIDER)
    private val deviceHasNetworkLocationProvider: Boolean get() = mgr.allProviders.contains(NETWORK_PROVIDER)

    private val locationListener = object : LocationUpdateListener {
        override fun onLocationChanged(location: Location) {
            if (isBetterLocation(location, lastLocation)) {
                lastLocation = location
                locationUpdateCallback(location)
            }
        }
    }

    private val singleLocationListener = object : LocationUpdateListener {
        override fun onLocationChanged(location: Location) {
            mgr.removeUpdates(this)
            locationUpdateCallback(location)
        }
    }

    @RequiresPermission(ACCESS_FINE_LOCATION)
    fun requestUpdates(minTime: Long, minDistance: Float) {
        if (deviceHasGPS)
            mgr.requestLocationUpdates(GPS_PROVIDER, minTime, minDistance, locationListener, Looper.getMainLooper())
        if (deviceHasNetworkLocationProvider)
            mgr.requestLocationUpdates(NETWORK_PROVIDER, minTime, minDistance, locationListener, Looper.getMainLooper())
    }

    @RequiresPermission(ACCESS_FINE_LOCATION)
    fun requestSingleUpdate() {
        if (deviceHasGPS)
            mgr.requestSingleUpdate(GPS_PROVIDER,  singleLocationListener, Looper.getMainLooper())
        if (deviceHasNetworkLocationProvider)
            mgr.requestSingleUpdate(NETWORK_PROVIDER, singleLocationListener, Looper.getMainLooper())
    }

    fun removeUpdates() {
        mgr.removeUpdates(locationListener)
        mgr.removeUpdates(singleLocationListener)
    }
}

// taken from https://developer.android.com/guide/topics/location/strategies.html#kotlin

private const val TWO_MINUTES: Long = 1000 * 60 * 2

/** Determines whether one Location reading is better than the current Location fix
 * @param location The new Location that you want to evaluate
 * @param currentBestLocation The current Location fix, to which you want to compare the new one
 */
private fun isBetterLocation(location: Location, currentBestLocation: Location?): Boolean {
    if (currentBestLocation == null) {
        // A new location is always better than no location
        return true
    }

    // Check whether the new location fix is newer or older
    val timeDelta: Long = location.time - currentBestLocation.time
    val isSignificantlyNewer: Boolean = timeDelta > TWO_MINUTES
    val isSignificantlyOlder:Boolean = timeDelta < -TWO_MINUTES
    val isNewer: Boolean = timeDelta > 0L

    // Check whether the new location fix is more or less accurate
    val accuracyDelta: Float = location.accuracy - currentBestLocation.accuracy
    val isLessAccurate: Boolean = accuracyDelta > 0f
    val isMoreAccurate: Boolean = accuracyDelta < 0f
    val isSignificantlyLessAccurate: Boolean = accuracyDelta > 200f

    // Check if the old and new location are from the same provider
    val isFromSameProvider: Boolean = location.provider == currentBestLocation.provider

    // Determine location quality using a combination of timeliness and accuracy
    return when {
        // the user has likely moved
        isSignificantlyNewer -> return true
        // If the new location is more than two minutes older, it must be worse
        isSignificantlyOlder -> return false
        isMoreAccurate -> true
        isNewer && !isLessAccurate -> true
        isNewer && !isSignificantlyLessAccurate && isFromSameProvider -> true
        else -> false
    }
}
