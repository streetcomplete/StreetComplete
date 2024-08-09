package de.westnordost.streetcomplete.screens.settings

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.StreetCompleteApplication
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestController
import de.westnordost.streetcomplete.data.preferences.ResurveyIntervalsUpdater
import de.westnordost.streetcomplete.data.visiblequests.DayNightQuestFilter
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderController
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeController
import de.westnordost.streetcomplete.screens.HasTitle
import de.westnordost.streetcomplete.util.dialogs.setViewWithDefaultPadding
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.ktx.hasPermission
import de.westnordost.streetcomplete.util.ktx.setUpToolbarTitleAndIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class QuestsSettingsFragment :
    PreferenceFragmentCompat(),
    HasTitle,
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val prefs: ObservableSettings by inject()
    private val resurveyIntervalsUpdater: ResurveyIntervalsUpdater by inject()
    private val visibleQuestTypeController: VisibleQuestTypeController by inject()
    private val dayNightQuestFilter: DayNightQuestFilter by inject()
    private val questTypeOrderController: QuestTypeOrderController by inject()

    override val title: String get() = getString(R.string.pref_screen_quests)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.rootView.findViewById<Toolbar>(R.id.toolbar)?.apply {
            setUpToolbarTitleAndIcon(this)
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        PreferenceManager.setDefaultValues(requireContext(), R.xml.preferences_ee_quests, false)
        addPreferencesFromResource(R.xml.preferences_ee_quests)

        findPreference<Preference>("advanced_resurvey")?.apply {
            isEnabled = prefs.getBoolean(Prefs.EXPERT_MODE, false)
            setOnPreferenceClickListener {
                val layout = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
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
                    .setViewWithDefaultPadding(layout)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        prefs.putString(Prefs.RESURVEY_DATE, dateEditText.text.toString())
                        prefs.putString(Prefs.RESURVEY_KEYS, keyEditText.text.toString())
                        resurveyIntervalsUpdater.update()
                    }
                    .show()
                true
            }
        }

        findPreference<Preference>(Prefs.QUEST_MONITOR)?.setOnPreferenceClickListener {
            val layout = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
            val enable = SwitchCompat(requireContext()).apply {
                isChecked = prefs.getBoolean(Prefs.QUEST_MONITOR, false)
                setText(R.string.pref_quest_monitor_title)
                setOnCheckedChangeListener { _, b ->
                    if (!b) return@setOnCheckedChangeListener
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity?.hasPermission(ACCESS_FINE_LOCATION) == false) {
                        isChecked = false
                        ActivityCompat.requestPermissions(requireActivity(), arrayOf(ACCESS_FINE_LOCATION), 0)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && activity?.hasPermission(ACCESS_BACKGROUND_LOCATION) == false)  {
                        isChecked = false
                        ActivityCompat.requestPermissions(requireActivity(), arrayOf(ACCESS_BACKGROUND_LOCATION), 0)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && activity?.hasPermission(POST_NOTIFICATIONS) == false) {
                        isChecked = false
                        ActivityCompat.requestPermissions(requireActivity(), arrayOf(POST_NOTIFICATIONS), 0)
                    }
                }
            }
            val downloadSwitch = SwitchCompat(requireContext()).apply {
                isChecked = prefs.getBoolean(Prefs.QUEST_MONITOR_DOWNLOAD, false)
                setText(R.string.pref_quest_monitor_download)
                setPadding(0, 0, 0, context.resources.dpToPx(8).toInt())
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
                .setViewWithDefaultPadding(ScrollView(context).apply { addView(layout) })
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    prefs.putBoolean(Prefs.QUEST_MONITOR, enable.isChecked)
                    prefs.putBoolean(Prefs.QUEST_MONITOR_GPS, gpsSwitch.isChecked)
                    prefs.putBoolean(Prefs.QUEST_MONITOR_NET, netSwitch.isChecked)
                    prefs.putBoolean(Prefs.QUEST_MONITOR_DOWNLOAD, downloadSwitch.isChecked)
                    prefs.putFloat(Prefs.QUEST_MONITOR_RADIUS, accuracyEditText.text.toString().toFloatOrNull() ?: 50f)
                }
                .show()
            true
        }

        findPreference<Preference>(Prefs.QUEST_SETTINGS_PER_PRESET)?.isEnabled = prefs.getBoolean(Prefs.EXPERT_MODE, false)
        findPreference<Preference>(Prefs.DYNAMIC_QUEST_CREATION)?.isEnabled = prefs.getBoolean(Prefs.EXPERT_MODE, false)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        when (key) {
            Prefs.DYNAMIC_QUEST_CREATION, Prefs.HIDE_OVERLAY_QUESTS -> {
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
        }
    }

    override fun onResume() {
        super.onResume()
        StreetCompleteApplication.preferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        StreetCompleteApplication.preferences.unregisterOnSharedPreferenceChangeListener(this)
    }

}
