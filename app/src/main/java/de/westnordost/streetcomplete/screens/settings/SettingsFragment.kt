package de.westnordost.streetcomplete.screens.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import de.westnordost.streetcomplete.ApplicationConstants.DELETE_OLD_DATA_AFTER
import de.westnordost.streetcomplete.ApplicationConstants.REFRESH_DATA_AFTER
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesDao
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController
import de.westnordost.streetcomplete.data.osmnotes.NoteController
import de.westnordost.streetcomplete.data.quest.QuestController
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.visiblequests.DayNightQuestFilter
import de.westnordost.streetcomplete.data.visiblequests.QuestPresetsSource
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeController
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeSource
import de.westnordost.streetcomplete.databinding.DialogDeleteCacheBinding
import de.westnordost.streetcomplete.screens.HasTitle
import de.westnordost.streetcomplete.screens.measure.MeasureActivity
import de.westnordost.streetcomplete.screens.settings.debug.ShowLinksActivity
import de.westnordost.streetcomplete.screens.settings.debug.ShowQuestFormsActivity
import de.westnordost.streetcomplete.util.getSelectedLocales
import de.westnordost.streetcomplete.util.ktx.format
import de.westnordost.streetcomplete.util.ktx.getYamlObject
import de.westnordost.streetcomplete.util.ktx.purge
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.util.setDefaultLocales
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.util.Locale

/** Shows the settings screen */
class SettingsFragment :
    PreferenceFragmentCompat(),
    HasTitle,
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val prefs: SharedPreferences by inject()
    private val downloadedTilesDao: DownloadedTilesDao by inject()
    private val noteController: NoteController by inject()
    private val mapDataController: MapDataController by inject()
    private val questController: QuestController by inject()
    private val resurveyIntervalsUpdater: ResurveyIntervalsUpdater by inject()
    private val questTypeRegistry: QuestTypeRegistry by inject()
    private val visibleQuestTypeSource: VisibleQuestTypeSource by inject()
    private val questPresetsSource: QuestPresetsSource by inject()
    private val visibleQuestTypeController: VisibleQuestTypeController by inject()
    private val dayNightQuestFilter: DayNightQuestFilter by inject()

    interface Listener {
        fun onClickedQuestSelection()
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    override val title: String get() = getString(R.string.action_settings)

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        PreferenceManager.setDefaultValues(requireContext(), R.xml.preferences, false)
        addPreferencesFromResource(R.xml.preferences)

        findPreference<Preference>("quests")?.setOnPreferenceClickListener {
            listener?.onClickedQuestSelection()
            true
        }

        findPreference<Preference>("delete_cache")?.setOnPreferenceClickListener {
            val dialogBinding = DialogDeleteCacheBinding.inflate(layoutInflater)
            dialogBinding.descriptionText.text = resources.getString(R.string.delete_cache_dialog_message2,
                (1.0 * REFRESH_DATA_AFTER / (24 * 60 * 60 * 1000)).format(Locale.getDefault(), 1),
                (1.0 * DELETE_OLD_DATA_AFTER / (24 * 60 * 60 * 1000)).format(Locale.getDefault(), 1)
            )
            AlertDialog.Builder(requireContext())
                .setView(dialogBinding.root)
                .setNeutralButton(R.string.delete_confirmation_both) { _, _ -> lifecycleScope.launch {
                    deleteTiles()
                    deleteCache()}
                }
                .setPositiveButton(R.string.delete_confirmation_tiles) { _, _ -> lifecycleScope.launch { deleteTiles() }}
                .setNegativeButton(R.string.delete_confirmation_data) { _, _ -> lifecycleScope.launch { deleteCache() }}
                .show()
            true
        }

        findPreference<Preference>("quests.restore.hidden")?.setOnPreferenceClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.restore_dialog_message)
                .setPositiveButton(R.string.restore_confirmation) { _, _ -> lifecycleScope.launch {
                    val hidden = questController.unhideAll()
                    context?.toast(getString(R.string.restore_hidden_success, hidden), Toast.LENGTH_LONG)
                } }
                .setNegativeButton(android.R.string.cancel, null)
                .show()

            true
        }

        findPreference<Preference>("debug")?.isVisible = BuildConfig.DEBUG

        findPreference<Preference>("debug.quests")?.setOnPreferenceClickListener {
            startActivity(Intent(context, ShowQuestFormsActivity::class.java))
            true
        }

        findPreference<Preference>("debug.links")?.setOnPreferenceClickListener {
            startActivity(Intent(context, ShowLinksActivity::class.java))
            true
        }

        findPreference<Preference>("debug.ar_measure_horizontal")?.setOnPreferenceClickListener {
            startActivity(MeasureActivity.createIntent(requireContext(), false))
            true
        }

        findPreference<Preference>("debug.ar_measure_vertical")?.setOnPreferenceClickListener {
            startActivity(MeasureActivity.createIntent(requireContext(), true))
            true
        }

        findPreference<Preference>("hide_notes_by")?.setOnPreferenceClickListener {
            val text = EditText(context)
            text.setText(prefs.getStringSet(Prefs.HIDE_NOTES_BY_USERS, emptySet())?.joinToString(","))
            text.setHint(R.string.pref_hide_notes_hint)
            text.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE

            val layout = LinearLayout(context)
            layout.setPadding(30,10,30,10)
            layout.addView(text)

            AlertDialog.Builder(requireContext())
                .setTitle(R.string.pref_hide_notes_message)
                .setView(layout)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val content = text.text.split(",").map { it.trim().lowercase() }.toSet()
                    prefs.edit().putStringSet(Prefs.HIDE_NOTES_BY_USERS, content).apply()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()

            true
        }

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

        buildLanguageSelector()
    }

    private fun buildLanguageSelector() {
        val entryValues = resources.getYamlObject<MutableList<String>>(R.raw.languages)
        val entries = entryValues.map {
            val locale = Locale.forLanguageTag(it)
            val name = locale.displayName
            val nativeName = locale.getDisplayName(locale)
            return@map nativeName + if (name != nativeName) " — $name" else ""
        }.toMutableList()

        // add default as first element
        entryValues.add(0, "")
        entries.add(0, getString(R.string.language_default))

        findPreference<ListPreference>("language.select")?.also {
            it.entries = entries.toTypedArray()
            it.entryValues = entryValues.toTypedArray()
        }
    }

    override fun onStart() {
        super.onStart()
        findPreference<Preference>("quests")?.summary = getQuestPreferenceSummary()
    }

    override fun onResume() {
        super.onResume()
        prefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        prefs.unregisterOnSharedPreferenceChangeListener(this)
    }

    @SuppressLint("InflateParams")
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            Prefs.AUTOSYNC -> {
                if (Prefs.Autosync.valueOf(prefs.getString(Prefs.AUTOSYNC, "ON")!!) != Prefs.Autosync.ON) {
                    AlertDialog.Builder(requireContext())
                        .setView(layoutInflater.inflate(R.layout.dialog_tutorial_upload, null))
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                }
            }
            Prefs.THEME_SELECT -> {
                val theme = Prefs.Theme.valueOf(prefs.getString(Prefs.THEME_SELECT, "AUTO")!!)
                AppCompatDelegate.setDefaultNightMode(theme.appCompatNightMode)
                activity?.let { ActivityCompat.recreate(it) }
            }
            Prefs.LANGUAGE_SELECT -> {
                setDefaultLocales(getSelectedLocales(requireContext()))
                activity?.let { ActivityCompat.recreate(it) }
            }
            Prefs.RESURVEY_INTERVALS -> {
                resurveyIntervalsUpdater.update()
            }
            Prefs.QUEST_GEOMETRIES -> {
                visibleQuestTypeController.clear()
            }
            Prefs.DAY_NIGHT_BEHAVIOR -> {
                dayNightQuestFilter.reload()
                visibleQuestTypeController.clear()
            }
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference is DialogPreferenceCompat) {
            val fragment = preference.createDialog()
            fragment.arguments = bundleOf("key" to preference.key)
            fragment.setTargetFragment(this, 0)
            fragment.show(parentFragmentManager, "androidx.preference.PreferenceFragment.DIALOG")
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    private suspend fun deleteCache() = withContext(Dispatchers.IO) {
        downloadedTilesDao.removeAll()
        mapDataController.clear()
        noteController.clear()
    }

    private suspend fun deleteTiles() = withContext(Dispatchers.IO) {
        context?.externalCacheDir?.purge()
    }

    private fun getQuestPreferenceSummary(): String {
        val presetName = questPresetsSource.selectedQuestPresetName ?: getString(R.string.quest_presets_default_name)
        val hasCustomPresets = questPresetsSource.getAll().isNotEmpty()
        val presetStr = if (hasCustomPresets) getString(R.string.pref_subtitle_quests_preset_name, presetName) + "\n" else ""

        val enabledCount = questTypeRegistry.filter { visibleQuestTypeSource.isVisible(it) }.count()
        val totalCount = questTypeRegistry.size
        val enabledStr = getString(R.string.pref_subtitle_quests, enabledCount, totalCount)

        return presetStr + enabledStr
    }
}
