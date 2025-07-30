package de.westnordost.streetcomplete.util.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import de.westnordost.streetcomplete.util.ktx.isLocationAvailable

/** Tells its listeners changes to whether the location is available, i.e. being updated. The
 *  location is available if there is both a GPS (or other location provider) fix and the user
 *  granted this application location permission.
 */
class LocationAvailabilityReceiver(private val context: Context) {

    private val listeners = mutableSetOf<(Boolean) -> Unit>()

    private val locationAvailabilityReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateLocationAvailability()
        }
    }

    private val requestLocationPermissionResultReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateLocationAvailability()
        }
    }

    init {
        context.registerReceiver(
            locationAvailabilityReceiver,
            IntentFilter(LocationManager.MODE_CHANGED_ACTION)
        )
        LocalBroadcastManager.getInstance(context).registerReceiver(
            requestLocationPermissionResultReceiver,
            IntentFilter(LocationRequestFragment.REQUEST_LOCATION_PERMISSION_RESULT)
        )
    }

    private fun updateLocationAvailability() {
        listeners.forEach { it.invoke(context.isLocationAvailable) }
    }

    fun addListener(listener: (Boolean) -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: (Boolean) -> Unit) {
        listeners.remove(listener)
    }
}
