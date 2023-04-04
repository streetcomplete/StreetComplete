package de.westnordost.streetcomplete.screens.settings

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestController
import de.westnordost.streetcomplete.data.visiblequests.DayNightQuestFilter
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderController
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeController
import de.westnordost.streetcomplete.screens.HasTitle
import de.westnordost.streetcomplete.util.ktx.awaitReceiverCall
import de.westnordost.streetcomplete.util.ktx.hasLocationPermission
import de.westnordost.streetcomplete.util.ktx.hasPermission
import de.westnordost.streetcomplete.util.ktx.isLocationEnabled
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import org.koin.android.ext.android.inject

class QuestsSettingsFragment :
    PreferenceFragmentCompat(),
    HasTitle,
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val prefs: SharedPreferences by inject()
    private val resurveyIntervalsUpdater: ResurveyIntervalsUpdater by inject()
    private val visibleQuestTypeController: VisibleQuestTypeController by inject()
    private val dayNightQuestFilter: DayNightQuestFilter by inject()
    private val questTypeOrderController: QuestTypeOrderController by inject()

    override val title: String get() = getString(R.string.pref_screen_quests)

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        PreferenceManager.setDefaultValues(requireContext(), R.xml.preferences_ee_quests, false)
        addPreferencesFromResource(R.xml.preferences_ee_quests)

        findPreference<Preference>("advanced_resurvey")?.apply {
            isEnabled = prefs.getBoolean(Prefs.EXPERT_MODE, false)
            setOnPreferenceClickListener {
                val layout = LinearLayout(context)
                layout.setPadding(30,10,30,10)
                layout.orientation = LinearLayout.VERTICAL
                val keyText = TextView(context)
                keyText.setText(R.string.advanced_resurvey_message_keys)
                val keyEditText = EditText(context)
                keyEditText.inputType = InputType.TYPE_CLASS_TEXT
                keyEditText.setHint(R.string.advanced_resurvey_hint_keys)
                keyEditText.setText(prefs.getString(Prefs.RESURVEY_KEYS, ""))

                val dateText = TextView(context)
                dateText.setText(R.string.advanced_resurvey_message_date)
                val dateEditText = EditText(context)
                dateEditText.inputType = InputType.TYPE_CLASS_TEXT
                dateEditText.setHint(R.string.advanced_resurvey_hint_date)
                dateEditText.setText(prefs.getString(Prefs.RESURVEY_DATE, ""))

                layout.addView(keyText)
                layout.addView(keyEditText)
                layout.addView(dateText)
                layout.addView(dateEditText)

                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.advanced_resurvey_title)
                    .setView(layout)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        prefs.edit {
                            putString(Prefs.RESURVEY_DATE, dateEditText.text.toString())
                            putString(Prefs.RESURVEY_KEYS, keyEditText.text.toString())
                        }
                        resurveyIntervalsUpdater.update()
                    }
                    .show()
                true
            }
        }

        findPreference<Preference>(Prefs.QUEST_MONITOR)?.setOnPreferenceClickListener {
            val layout = LinearLayout(context)
            layout.setPadding(30, 10, 30, 10)
            layout.orientation = LinearLayout.VERTICAL
            val enable = SwitchCompat(requireContext()).apply {
                isChecked = prefs.getBoolean(Prefs.QUEST_MONITOR, false)
                setText(R.string.pref_quest_monitor_title)
            }
            val downloadSwitch = SwitchCompat(requireContext()).apply {
                isChecked = prefs.getBoolean(Prefs.QUEST_MONITOR_DOWNLOAD, false)
                setText(R.string.pref_quest_monitor_download)
                setPadding(0, 0, 0, 10)
            }
            val activeText = TextView(context).apply { setText(R.string.quest_monitor_active_request) }
            val gpsSwitch = SwitchCompat(requireContext()).apply {
                isChecked = prefs.getBoolean(Prefs.QUEST_MONITOR_GPS, false)
                setText(R.string.quest_monitor_gps)
            }
            val netSwitch = SwitchCompat(requireContext()).apply {
                isChecked = prefs.getBoolean(Prefs.QUEST_MONITOR_NET, false)
                setText(R.string.quest_monitor_net)
            }
            val accuracyText = TextView(context).apply { setText(R.string.quest_monitor_search_radius_text) }
            val accuracyEditText = EditText(context)
            accuracyEditText.inputType = InputType.TYPE_CLASS_NUMBER
            accuracyEditText.setText(prefs.getFloat(Prefs.QUEST_MONITOR_RADIUS, 50f).toString())

            layout.addView(enable)
            layout.addView(downloadSwitch)
            layout.addView(activeText)
            layout.addView(gpsSwitch)
            layout.addView(netSwitch)
            layout.addView(accuracyText)
            layout.addView(accuracyEditText)

            AlertDialog.Builder(requireContext())
                .setTitle(R.string.pref_quest_monitor_title)
                .setView(ScrollView(context).apply { addView(layout) })
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    prefs.edit {
                        putBoolean(Prefs.QUEST_MONITOR, enable.isChecked)
                        putBoolean(Prefs.QUEST_MONITOR_GPS, gpsSwitch.isChecked)
                        putBoolean(Prefs.QUEST_MONITOR_NET, netSwitch.isChecked)
                        putBoolean(Prefs.QUEST_MONITOR_DOWNLOAD, downloadSwitch.isChecked)
                        putFloat(Prefs.QUEST_MONITOR_RADIUS, accuracyText.text.toString().toFloatOrNull() ?: 50f)
                    }
                }
                .show()
            true
        }

        findPreference<Preference>(Prefs.QUEST_SETTINGS_PER_PRESET)?.isEnabled = prefs.getBoolean(Prefs.EXPERT_MODE, false)
        findPreference<Preference>(Prefs.DYNAMIC_QUEST_CREATION)?.isEnabled = prefs.getBoolean(Prefs.EXPERT_MODE, false)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            Prefs.DYNAMIC_QUEST_CREATION -> {
                lifecycleScope.launch(Dispatchers.IO) { visibleQuestTypeController.onQuestTypeVisibilitiesChanged() }
            }
            Prefs.DAY_NIGHT_BEHAVIOR -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    dayNightQuestFilter.reload()
                    visibleQuestTypeController.onQuestTypeVisibilitiesChanged()
                    questTypeOrderController.onQuestTypeOrderChanged()
                }
            }
            Prefs.QUEST_SETTINGS_PER_PRESET -> { lifecycleScope.launch(Dispatchers.IO) { OsmQuestController.reloadQuestTypes() } }
            Prefs.QUEST_MONITOR -> {
                if (!prefs.getBoolean(key, false)) return
                // Q introduces background location permission, but only R+ need it for foreground service
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val requester = LocationRequester(requireActivity(), this)
                    lifecycleScope.launch {
                        if (!requester.requestBackgroundLocationPermission())
                            prefs.edit { putBoolean(key, false) }
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ActivityCompat.requestPermissions(requireActivity(), arrayOf(POST_NOTIFICATIONS), 0) // don't care about result. if the user wants notifications and denies permission so be it.
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        prefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        prefs.unregisterOnSharedPreferenceChangeListener(this)
    }

}

// Use the old location request thing that has been removed from SC because it sometimes may be
// unreliable. Reasoning: less work, no possibility for testing background location permission,
// it still mostly works, and it's only used for the nearby quest monitor.
private class LocationRequester(private val activity: Activity, activityResultCaller: ActivityResultCaller) {

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

    suspend fun requestBackgroundLocationPermission(): Boolean {
        if (activity.hasPermission(ACCESS_BACKGROUND_LOCATION) || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return true
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, ACCESS_BACKGROUND_LOCATION)) {
            if (!askUserToAcknowledgeLocationPermissionRationale()) {
                return false
            }
        }
        val result = requestPermission(ACCESS_BACKGROUND_LOCATION)
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

    private suspend fun askUserToAcknowledgeLocationPermissionRationale(background: Boolean = false): Boolean =
        suspendCancellableCoroutine { cont ->
            val dlg = AlertDialog.Builder(activity)
                .setTitle(R.string.no_location_permission_warning_title)
                .setMessage(if (background) R.string.quest_monitor_permission_warning else R.string.no_location_permission_warning)
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

@Deprecated("see #3967")
private class ActivityForResultLauncher<I, O> (
    caller: ActivityResultCaller,
    contract: ActivityResultContract<I, O>
) {
    private var continuation: CancellableContinuation<O>? = null
    private val launcher = caller.registerForActivityResult(contract) { continuation?.resume(it) }

    suspend operator fun invoke(input: I): O = suspendCancellableCoroutine {
        continuation?.cancel()
        continuation = it
        launcher.launch(input)
        it.invokeOnCancellation {
            continuation = null
        }
    }
}
