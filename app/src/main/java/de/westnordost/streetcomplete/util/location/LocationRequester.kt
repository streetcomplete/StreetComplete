package de.westnordost.streetcomplete.util.location

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.provider.Settings
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.ActivityForResultLauncher
import de.westnordost.streetcomplete.util.ktx.awaitReceiverCall
import de.westnordost.streetcomplete.util.ktx.hasLocationPermission
import de.westnordost.streetcomplete.util.ktx.isLocationEnabled
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** Manages the process to ensure that the app can access the user's location. Two steps:
 *
 *  1. ask for permission
 *  2. ask for location to be turned on
 *
 * Reports back via a local broadcast with the intent RequireLocation.REQUEST_LOCATION_PERMISSION_RESULT */
class LocationRequester(private val activity: Activity, activityResultCaller: ActivityResultCaller) {

    private val requestPermission = ActivityForResultLauncher(
        activityResultCaller, ActivityResultContracts.RequestPermission()
    )
    private val startActivityForResult = ActivityForResultLauncher(
        activityResultCaller, ActivityResultContracts.StartActivityForResult()
    )

    suspend operator fun invoke(): Boolean =
        requireLocationPermission() && requireLocationEnabled()

    private suspend fun requireLocationPermission(): Boolean =
        activity.hasLocationPermission || requestLocationPermission()

    private suspend fun requireLocationEnabled(): Boolean =
        activity.isLocationEnabled || requestEnableLocation()

    private suspend fun requestLocationPermission(): Boolean {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, ACCESS_FINE_LOCATION)) {
            if (!askUserToAcknowledgeLocationPermissionRationale()) {
                return false
            }
        }
        val result = requestPermission(ACCESS_FINE_LOCATION)
        /* There is no Android system broadcast when a permission is granted or declined, so we create an own one */
        broadcastPermissionRequestResult(result)
        return result
    }

    private suspend fun requestEnableLocation(): Boolean = coroutineScope {
        /* the user may turn on location in the pull-down-overlay, without actually going into
           settings screen, so checking for updated location-enablement must be done in parallel with
           asking the user to go into the settings screen to do that. Whatever finishes first must
           cancel the other  */
        val locationEnabledJob = async { awaitLocationEnablementChanged() }
        val locationSettingsJob = async { openLocationSettings() }
        locationEnabledJob.invokeOnCompletion { locationSettingsJob.cancel() }
        locationSettingsJob.invokeOnCompletion { locationEnabledJob.cancel() }
        return@coroutineScope activity.isLocationEnabled
    }

    private suspend fun awaitLocationEnablementChanged() {
        activity.awaitReceiverCall(IntentFilter(LocationManager.MODE_CHANGED_ACTION))
    }

    private suspend fun openLocationSettings() {
        if (askUserToOpenLocationSettings()) {
            launchLocationSettings()
        }
    }

    private suspend fun launchLocationSettings() =
        startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))

    private suspend fun askUserToAcknowledgeLocationPermissionRationale(): Boolean =
        suspendCancellableCoroutine { cont ->
            val dlg = AlertDialog.Builder(activity)
                .setTitle(R.string.no_location_permission_warning_title)
                .setMessage(R.string.no_location_permission_warning)
                .setPositiveButton(android.R.string.ok) { _, _ -> cont.resume(true) }
                .setNegativeButton(android.R.string.cancel) { _, _ -> cont.resume(false) }
                .setOnCancelListener { cont.resume(false) }
                .create()
            dlg.show()
            cont.invokeOnCancellation { dlg.cancel() }
        }

    private suspend fun askUserToOpenLocationSettings(): Boolean =
        suspendCancellableCoroutine { cont ->
            val dlg = AlertDialog.Builder(activity)
                .setMessage(R.string.turn_on_location_request)
                .setPositiveButton(android.R.string.ok) { _, _ -> cont.resume(true) }
                .setNegativeButton(android.R.string.cancel) { _, _ -> cont.resume(false) }
                .setOnCancelListener { cont.resume(false) }
                .create()
            dlg.show()
            cont.invokeOnCancellation { dlg.cancel() }
        }

    private fun broadcastPermissionRequestResult(result: Boolean) {
        val intent = Intent(REQUEST_LOCATION_PERMISSION_RESULT)
        intent.putExtra(GRANTED, result)
        /* A use of a broadcast is deliberate because it should mimic the behavior of Android system
           calls such as that the connectivity changed. There are components all over the app that
           want to know when location is available */
        LocalBroadcastManager.getInstance(activity).sendBroadcast(intent)
    }

    companion object {
        const val REQUEST_LOCATION_PERMISSION_RESULT = "permissions.REQUEST_LOCATION_PERMISSION_RESULT"
        const val GRANTED = "granted"
    }
}
