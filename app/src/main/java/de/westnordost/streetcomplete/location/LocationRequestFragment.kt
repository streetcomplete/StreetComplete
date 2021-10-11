package de.westnordost.streetcomplete.location

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import de.westnordost.streetcomplete.R

/** Manages the process to ensure that the app can access the user's location. Two steps:
 *
 *  1. ask for permission
 *  2. ask for location to be turned on
 *
 * This fragment reports back via a local broadcast with the intent LocationRequestFragment.ACTION_FINISHED
 * The process is started via [.startRequest]  */
class LocationRequestFragment : Fragment() {

    var state: LocationState? = null
    private set

    private var inProgress = false
    private var locationProviderChangedReceiver: BroadcastReceiver? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(), ::onRequestLocationPermissionsResult
    )
    private val startActivityForResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(), ::onRequestLocationSettingsToBeOnResult
    )

    /* Lifecycle */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            val stateName = savedInstanceState.getString("locationState")
            if (stateName != null) state = LocationState.valueOf(stateName)
            inProgress = savedInstanceState.getBoolean("inProgress")
        }
    }


    override fun onStop() {
        super.onStop()
        unregisterForLocationProviderChanges()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (state != null) outState.putString("locationState", state!!.name)
        outState.putBoolean("inProgress", inProgress)
    }

    /** Start location request process. When already started, will not be started again.  */
    fun startRequest() {
        if (!inProgress) {
            inProgress = true
            state = null
            nextStep()
        }
    }

    private fun nextStep() {
        if (state == null || state == LocationState.DENIED) {
            checkLocationPermissions()
        } else if (state == LocationState.ALLOWED) {
            checkLocationSettings()
        } else if (state == LocationState.ENABLED) {
            finish()
        }
    }

    private fun finish() {
        inProgress = false
        val intent = Intent(ACTION_FINISHED)
        intent.putExtra(STATE, state!!.name)
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
    }

    /* Step 1: Ask for permission */

    private fun checkLocationPermissions() {
        if (requireContext().hasLocationPermission) {
            state = LocationState.ALLOWED
            nextStep()
        } else {
            requestLocationPermissions()
        }
    }

    private fun requestLocationPermissions() {
        val activity = requireActivity()
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, ACCESS_FINE_LOCATION)) {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.no_location_permission_warning_title)
                .setMessage(R.string.no_location_permission_warning)
                .setPositiveButton(android.R.string.ok) { _, _ -> requestPermissionLauncher.launch(ACCESS_FINE_LOCATION) }
                .setNegativeButton(android.R.string.cancel) { _, _ -> deniedLocationPermissions() }
                .setOnCancelListener { deniedLocationPermissions() }
                .show()
        } else {
            requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
        }
    }

    private fun onRequestLocationPermissionsResult(isGranted: Boolean) {
        if (isGranted) checkLocationPermissions()
        else deniedLocationPermissions()
    }

    private fun deniedLocationPermissions() {
        state = LocationState.DENIED
        finish()
    }

    /* Step 1: Ask for location to be turned on */

    private fun checkLocationSettings() {
        if (requireContext().isLocationEnabled) {
            state = LocationState.ENABLED
            nextStep()
        } else {
            requestLocationSettingsToBeOn()
        }
    }

    private fun requestLocationSettingsToBeOn() {
        val dlg = AlertDialog.Builder(requireContext())
            .setMessage(R.string.turn_on_location_request)
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()
                startActivityForResultLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> cancelTurnLocationOnDialog() }
            .setOnCancelListener { cancelTurnLocationOnDialog() }
            .create()

        // the user may turn on location in the pull-down-overlay, without actually going into
        // settings dialog
        registerForLocationProviderChanges(dlg)
        dlg.show()
    }

    private fun onRequestLocationSettingsToBeOnResult(result: ActivityResult) {
        // we ignore the resultCode, because we always get Activity.RESULT_CANCELED. Instead, we
        // check if the conditions are fulfilled now
        checkLocationSettings()
    }

    private fun registerForLocationProviderChanges(dlg: AlertDialog) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                dlg.dismiss()
                unregisterForLocationProviderChanges()
                checkLocationSettings()
            }
        }
        requireContext().registerReceiver(receiver, createLocationAvailabilityIntentFilter())
        locationProviderChangedReceiver = receiver
    }

    private fun unregisterForLocationProviderChanges() {
        locationProviderChangedReceiver?.let { receiver ->
            requireContext().unregisterReceiver(receiver)
        }
        locationProviderChangedReceiver = null
    }

    private fun cancelTurnLocationOnDialog() {
        unregisterForLocationProviderChanges()
        finish()
    }

    companion object {
        const val ACTION_FINISHED = "de.westnordost.LocationRequestFragment.FINISHED"
        const val STATE = "state"
    }
}
