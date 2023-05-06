package de.westnordost.streetcomplete.util.location

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.ktx.hasLocationPermission
import de.westnordost.streetcomplete.util.ktx.isLocationEnabled

/** Manages the process to ensure that the app can access the user's location. Two steps:
 *
 *  1. ask for permission
 *  2. ask for location to be turned on
 *
 *  Reports back via a local broadcast with the intent RequireLocation.REQUEST_LOCATION_PERMISSION_RESULT
 * */
class LocationRequestFragment : Fragment() {

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
        ::onPermissionRequestResult
    )
    private val openLocationSettings = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ::onReturnFromLocationSettings
    )

    private enum class State { DENIED, ALLOWED, ENABLED }
    private var state: State? = null
    private var inProgress = false

    private var locationProviderChangedReceiver: BroadcastReceiver? = null

    /* Lifecycle */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.getString(STATE)?.let { State.valueOf(it) }
        inProgress = savedInstanceState?.getBoolean(IN_PROGRESS) ?: false
    }

    override fun onStop() {
        super.onStop()
        unregisterForLocationProviderChanges()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        state?.let { outState.putString(STATE, it.name) }
        outState.putBoolean(IN_PROGRESS, inProgress)
    }

    /** Start location request process. When already started, will not be started again.  */
    fun startRequest() {
        if (inProgress) return
        inProgress = true
        state = null
        nextStep()
    }

    private fun nextStep() {
        when (state) {
            null, State.DENIED -> requirePermission()
            State.ALLOWED -> requireLocationEnabled()
            State.ENABLED -> finish()
        }
    }

    private fun finish() {
        inProgress = false
        broadcastPermissionRequestResult(state == State.ENABLED)
    }

    private fun broadcastPermissionRequestResult(result: Boolean) {
        val intent = Intent(REQUEST_LOCATION_PERMISSION_RESULT)
        intent.putExtra(GRANTED, result)
        /* A use of a broadcast is deliberate because it should mimic the behavior of Android system
           calls such as that the connectivity changed. There are components all over the app that
           want to know when location is available */
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
    }

    /* Step 1: Ask for permission */

    private fun requirePermission() {
        if (requireContext().hasLocationPermission) {
            state = State.ALLOWED
            nextStep()
        } else {
            requestPermission()
        }
    }

    private fun requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), ACCESS_FINE_LOCATION)) {
            showRequestPermissionRationale { requestPermission.launch(ACCESS_FINE_LOCATION) }
        } else {
            requestPermission.launch(ACCESS_FINE_LOCATION)
        }
    }

    private fun showRequestPermissionRationale(onOkay: () -> Unit) {
        AlertDialog.Builder(requireActivity())
            .setTitle(R.string.no_location_permission_warning_title)
            .setMessage(R.string.no_location_permission_warning)
            .setPositiveButton(android.R.string.ok) { _, _ -> onOkay() }
            .show()
    }

    private fun onPermissionRequestResult(isGranted: Boolean) {
        if (isGranted) {
            requirePermission() // retry then...
        } else {
            state = State.DENIED
            finish()
        }
    }

    /* Step 2: Ask for location to be turned on */

    private fun requireLocationEnabled() {
        if (requireContext().isLocationEnabled) {
            state = State.ENABLED
            nextStep()
        } else {
            askUserToEnableLocation()
        }
    }

    private fun askUserToEnableLocation() {
        val dlg = AlertDialog.Builder(requireContext())
            .setMessage(R.string.turn_on_location_request)
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()
                openLocationSettings.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> cancelEnableLocationDialog() }
            .setOnCancelListener { cancelEnableLocationDialog() }
            .create()

        // the user may turn on location in the pull-down-overlay, without actually going into
        // settings dialog
        registerForLocationProviderChanges(dlg)
        dlg.show()
    }

    private fun cancelEnableLocationDialog() {
        unregisterForLocationProviderChanges()
        finish()
    }

    private fun onReturnFromLocationSettings(activityResult: ActivityResult) {
        // we ignore the resultCode, because we always get Activity.RESULT_CANCELED. Instead, we
        // check if the conditions are fulfilled now
        requireLocationEnabled()
    }

    private fun registerForLocationProviderChanges(dlg: AlertDialog) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                dlg.dismiss()
                unregisterForLocationProviderChanges()
                requireLocationEnabled()
            }
        }
        requireContext().registerReceiver(receiver, IntentFilter(LocationManager.MODE_CHANGED_ACTION))
        locationProviderChangedReceiver = receiver
    }

    private fun unregisterForLocationProviderChanges() {
        locationProviderChangedReceiver?.let { requireContext().unregisterReceiver(it) }
        locationProviderChangedReceiver = null
    }

    companion object {
        const val REQUEST_LOCATION_PERMISSION_RESULT = "permissions.REQUEST_LOCATION_PERMISSION_RESULT"
        const val GRANTED = "granted"

        private const val STATE = "state"
        private const val IN_PROGRESS = "in_progress"
    }
}
