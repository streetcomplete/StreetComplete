package de.westnordost.streetcomplete.screens.settings

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestController
import de.westnordost.streetcomplete.data.visiblequests.DayNightQuestFilter
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeController
import de.westnordost.streetcomplete.screens.HasTitle
import de.westnordost.streetcomplete.util.location.LocationRequester
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class QuestsSettingsFragment :
    PreferenceFragmentCompat(),
    HasTitle,
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val prefs: SharedPreferences by inject()
    private val resurveyIntervalsUpdater: ResurveyIntervalsUpdater by inject()
    private val visibleQuestTypeController: VisibleQuestTypeController by inject()
    private val dayNightQuestFilter: DayNightQuestFilter by inject()

    override val title: String get() = getString(R.string.pref_screen_quests)

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        PreferenceManager.setDefaultValues(requireContext(), R.xml.preferences_ee_quests, false)
        addPreferencesFromResource(R.xml.preferences_ee_quests)

        findPreference<Preference>("advanced_resurvey")?.setOnPreferenceClickListener {
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            Prefs.DYNAMIC_QUEST_CREATION -> {
                visibleQuestTypeController.onQuestTypeVisibilitiesChanged()
            }
            Prefs.DAY_NIGHT_BEHAVIOR -> {
                dayNightQuestFilter.reload()
                visibleQuestTypeController.onQuestTypeVisibilitiesChanged()
                // todo: (re)initializeQuestTypeOrders somehow, maybe just set inactive then active in quest
            }
            Prefs.QUEST_SETTINGS_PER_PRESET -> { OsmQuestController.reloadQuestTypes() }
            Prefs.QUEST_MONITOR -> {
                // Q introduces background location permission, but only R+ need it for foreground service
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && prefs.getBoolean(key, false)) {
                    val requester = LocationRequester(requireActivity(), this)
                    lifecycleScope.launch {
                        if (!requester.requestBackgroundLocationPermission())
                            prefs.edit { putBoolean(key, false) }
                    }
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
