package de.westnordost.streetcomplete.location

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.location.LocationManager.GPS_PROVIDER
import android.location.LocationManager.NETWORK_PROVIDER
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.location.LocationManagerCompat
import androidx.core.os.CancellationSignal
import androidx.core.util.Consumer

/** Convenience wrapper around the location manager with easier API, making use of both the GPS
 *  and Network provider */
class FineLocationManager(context: Context, locationUpdateCallback: (Location) -> Unit) {
    private val locationManager = context.getSystemService<LocationManager>()!!
    private val mainExecutor = ContextCompat.getMainExecutor(context)
    private val currentLocationConsumer = Consumer<Location?> {
        if (it != null) {
            if (!networkCancellationSignal.isCanceled && !gpsCancellationSignal.isCanceled) {
                locationUpdateCallback(it)
            }
        }
    }
    private var gpsCancellationSignal = CancellationSignal()
    private var networkCancellationSignal = CancellationSignal()
    private var lastLocation: Location? = null

    private val deviceHasGPS: Boolean get() = locationManager.allProviders.contains(GPS_PROVIDER)
    private val deviceHasNetworkLocationProvider: Boolean get() = locationManager.allProviders.contains(NETWORK_PROVIDER)

    private val locationListener = object : LocationUpdateListener {
        override fun onLocationChanged(location: Location) {
            if (location.isBetterThan(lastLocation)) {
                lastLocation = location
                locationUpdateCallback(location)
            }
        }
    }

    // Both signals are refreshed regardless of whether the device has both providers, because
    // they are both canceled in removeUpdates and both checked in the locationListener
    private fun refreshCancellationSignals() {
        if (gpsCancellationSignal.isCanceled) {
            gpsCancellationSignal = CancellationSignal()
        }
        if (networkCancellationSignal.isCanceled) {
            networkCancellationSignal = CancellationSignal()
        }
    }

    @RequiresPermission(ACCESS_FINE_LOCATION)
    fun requestUpdates(minTime: Long, minDistance: Float) {
        if (deviceHasGPS)
            locationManager.requestLocationUpdates(GPS_PROVIDER, minTime, minDistance, locationListener, Looper.getMainLooper())
        if (deviceHasNetworkLocationProvider)
            locationManager.requestLocationUpdates(NETWORK_PROVIDER, minTime, minDistance, locationListener, Looper.getMainLooper())
    }

    @RequiresPermission(ACCESS_FINE_LOCATION)
    @Synchronized fun getCurrentLocation() {
        refreshCancellationSignals()
        if (deviceHasGPS) {
            LocationManagerCompat.getCurrentLocation(
                locationManager, GPS_PROVIDER, gpsCancellationSignal, mainExecutor, currentLocationConsumer
            )
        }
        if (deviceHasNetworkLocationProvider) {
            LocationManagerCompat.getCurrentLocation(
                locationManager, NETWORK_PROVIDER, networkCancellationSignal, mainExecutor, currentLocationConsumer
            )
        }
    }

    @Synchronized fun removeUpdates() {
        locationManager.removeUpdates(locationListener)
        gpsCancellationSignal.cancel()
        networkCancellationSignal.cancel()
    }
}

// Based on https://web.archive.org/web/20180424190538/https://developer.android.com/guide/topics/location/strategies.html#BestEstimate

private const val TWO_MINUTES = 1000L * 60 * 2

/** Determines whether this Location reading is better than the previous Location fix */
private fun Location.isBetterThan(previous: Location?): Boolean {
    // Check whether this is a valid location at all.
    // Happened once that lat/lon is NaN, maybe issue of that particular device
    if (this.longitude.isNaN() || this.latitude.isNaN()) return false

    // A new location is always better than no location
    if (previous == null) return true

    // Check whether the new location fix is newer or older
    val timeDelta = this.time - previous.time
    val isMuchNewer = timeDelta > TWO_MINUTES
    val isMuchOlder = timeDelta < -TWO_MINUTES
    val isNewer = timeDelta > 0L

    // Check whether the new location fix is more or less accurate
    val accuracyDelta = this.accuracy - previous.accuracy
    val isLessAccurate = accuracyDelta > 0f
    val isMoreAccurate = accuracyDelta < 0f
    val isMuchLessAccurate = accuracyDelta > 200f

    val isFromSameProvider = this.provider == previous.provider

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
