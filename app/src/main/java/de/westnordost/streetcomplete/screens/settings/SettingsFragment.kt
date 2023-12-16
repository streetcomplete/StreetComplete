package de.westnordost.streetcomplete.screens.settings

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.ApplicationConstants.DELETE_OLD_DATA_AFTER_DAYS
import de.westnordost.streetcomplete.ApplicationConstants.REFRESH_DATA_AFTER
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesController
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestController
import de.westnordost.streetcomplete.data.osmnotes.NoteController
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestController
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuestController
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.visiblequests.QuestPreset
import de.westnordost.streetcomplete.data.visiblequests.QuestPresetsSource
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeSource
import de.westnordost.streetcomplete.databinding.DialogDeleteCacheBinding
import de.westnordost.streetcomplete.screens.HasTitle
import de.westnordost.streetcomplete.screens.TwoPaneListFragment
import de.westnordost.streetcomplete.screens.settings.debug.ShowLinksActivity
import de.westnordost.streetcomplete.screens.settings.debug.ShowQuestFormsActivity
import de.westnordost.streetcomplete.util.TempLogger
import de.westnordost.streetcomplete.util.getDefaultTheme
import de.westnordost.streetcomplete.util.getSelectedLocales
import de.westnordost.streetcomplete.util.ktx.format
import de.westnordost.streetcomplete.util.ktx.getYamlObject
import de.westnordost.streetcomplete.util.ktx.minusInSystemTimeZone
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.ktx.purge
import de.westnordost.streetcomplete.util.ktx.setUpToolbarTitleAndIcon
import de.westnordost.streetcomplete.util.ktx.systemTimeNow
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.prefs.Preferences
import de.westnordost.streetcomplete.util.setDefaultLocales
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.android.ext.android.inject
import java.util.Locale

/** Shows the settings lists */
class SettingsFragment : TwoPaneListFragment(), HasTitle {

    private val prefs: Preferences by inject()
    private val downloadedTilesController: DownloadedTilesController by inject()
    private val noteController: NoteController by inject()
    private val mapDataController: MapDataController by inject()
    private val osmQuestController: OsmQuestController by inject()
    private val osmNoteQuestController: OsmNoteQuestController by inject()
    private val resurveyIntervalsUpdater: ResurveyIntervalsUpdater by inject()
    private val questTypeRegistry: QuestTypeRegistry by inject()
    private val visibleQuestTypeSource: VisibleQuestTypeSource by inject()
    private val questPresetsSource: QuestPresetsSource by inject()
    private val externalSourceQuestController: ExternalSourceQuestController by inject()

    override val title: String get() = getString(R.string.action_settings)

    private val visibleQuestTypeListener = object : VisibleQuestTypeSource.Listener {
        override fun onQuestTypeVisibilityChanged(questType: QuestType, visible: Boolean) {
            setQuestPreferenceSummary()
        }

        override fun onQuestTypeVisibilitiesChanged() {
            setQuestPreferenceSummary()
        }
    }

    private val questPresetsListener = object : QuestPresetsSource.Listener {
        override fun onSelectedQuestPresetChanged() {
            setQuestPresetsPreferenceSummary()
        }

        override fun onAddedQuestPreset(preset: QuestPreset) {
            setQuestPresetsPreferenceSummary()
        }

        override fun onRenamedQuestPreset(preset: QuestPreset) {
            setQuestPresetsPreferenceSummary()
        }

        override fun onDeletedQuestPreset(presetId: Long) {
            setQuestPresetsPreferenceSummary()
        }
    }

    private val onAutosyncChanged =  {
        if (Prefs.Autosync.valueOf(prefs.getStringOrNull(Prefs.AUTOSYNC) ?: "ON") != Prefs.Autosync.ON) {
            AlertDialog.Builder(requireContext())
                .setView(layoutInflater.inflate(R.layout.dialog_tutorial_upload, null))
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
    }

    private val onThemeChanged = {
        val theme = Prefs.Theme.valueOf(prefs.getStringOrNull(Prefs.THEME_SELECT) ?: getDefaultTheme())
        AppCompatDelegate.setDefaultNightMode(theme.appCompatNightMode)
        activity?.let { ActivityCompat.recreate(it) }
        Unit
    }

    private val onLanguageChanged = {
        setDefaultLocales(getSelectedLocales(prefs))
        activity?.let { ActivityCompat.recreate(it) }
        Unit
    }

    private val onResurveyIntervalsChanged = {
        resurveyIntervalsUpdater.update()
        if (prefs.getBoolean(Prefs.DYNAMIC_QUEST_CREATION, false))
            OsmQuestController.reloadQuestTypes()
    }

    private val onExpertModeChanged = {
        if (prefs.getBoolean(Prefs.EXPERT_MODE, false)) {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.general_warning)
                .setMessage(R.string.pref_expert_mode_message)
                .setPositiveButton(R.string.dialog_button_understood, null)
                .setNegativeButton(android.R.string.cancel) { d, _ -> d.cancel() }
                .setOnCancelListener { findPreference<SwitchPreference>(Prefs.EXPERT_MODE)?.isChecked = false }
                .show()
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        PreferenceManager.setDefaultValues(requireContext(), R.xml.preferences, false)
        addPreferencesFromResource(R.xml.preferences)

        findPreference<NumberPickerPreference>("map.tilecache")?.setSummaryProvider { pref ->
            requireContext().getString(R.string.pref_tilecache_size_summary, (pref as NumberPickerPreference).value)
        }

        findPreference<Preference>("delete_cache")?.setOnPreferenceClickListener {
            val dialogBinding = DialogDeleteCacheBinding.inflate(layoutInflater)
            dialogBinding.descriptionText.text = resources.getString(R.string.delete_cache_dialog_message2,
                (1.0 * REFRESH_DATA_AFTER / (24 * 60 * 60 * 1000)).format(Locale.getDefault(), 1),
                (1.0 * prefs.getInt(Prefs.DATA_RETAIN_TIME, DELETE_OLD_DATA_AFTER_DAYS)).format(Locale.getDefault(), 1)
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
                .setMessage(R.string.restore_dialog_hint)
                .setPositiveButton(R.string.restore_confirmation) { _, _ -> lifecycleScope.launch {
                    val hidden = unhideQuests()
                    context?.toast(getString(R.string.restore_hidden_success, hidden), Toast.LENGTH_LONG)
                } }
                .setNegativeButton(android.R.string.cancel, null)
                .show()

            true
        }

        // todo: remove pref and related strings
        findPreference<Preference>("read_log")?.setOnPreferenceClickListener {
            var reversed = false
            var filter = "" // todo: separate filter by level or tag?
            var maxLines = 200
            val log = TextView(requireContext())
            var lines = TempLogger.getLog().take(maxLines)
            log.setTextIsSelectable(true)
            log.text = lines.joinToString("\n")
            fun reloadText() {
                val l = TempLogger.getLog()
                lines = when {
                    filter.isNotBlank() && reversed -> l.asReversed().filter { line -> line.toString().contains(filter, true) }
                    filter.isNotBlank() -> l.filter { line -> line.toString().contains(filter, true) }
                    reversed -> l.asReversed()
                    else -> l
                }.take(maxLines)
                log.text = lines.joinToString("\n")
            }
            val scrollLog = ScrollView(requireContext()).apply {
                addView(log)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    setOnScrollChangeListener { _, _, _, _, _ ->
                        if (log.bottom <= height + scrollY && lines.size >= maxLines) {
                            maxLines *= 2
                            reloadText()
                        }
                    }
                }
            }
            val reverseButton = Button(requireContext())
            reverseButton.setText(R.string.pref_read_reverse_button)
            reverseButton.setOnClickListener {
                reversed = !reversed
                reloadText()
                scrollLog.scrollY = 0
            }
            val filterView = EditText(requireContext()).apply {
                setHint(R.string.pref_read_filter_hint)
                doAfterTextChanged {
                    filter = it.toString()
                    val previousCursorPosition = selectionStart
                    reloadText()
                    scrollLog.fullScroll(View.FOCUS_UP)
                    requestFocus() // focus is lost when scrolling it seems
                    setSelection(previousCursorPosition)
                }
                setPadding(30, 10, 30, 10)
            }
            val layout = LinearLayout(requireContext()).apply { orientation = LinearLayout.VERTICAL }
            layout.addView(LinearLayout(requireContext()).apply {
                addView(reverseButton)
                addView(filterView)
            }) // put this on top, or layout will need more work to keep this visible
            layout.addView(scrollLog)
            val d = AlertDialog.Builder(requireContext())
                .setTitle(R.string.pref_read_log_title)
                .setView(layout) // not using default padding to allow longer log lines (looks ugly, but is very convenient)
                .setPositiveButton(R.string.close, null)
                .setNegativeButton(R.string.pref_read_log_save) { _, _ ->
                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        val fileName = "log_${nowAsEpochMilliseconds()}.txt"
                        putExtra(Intent.EXTRA_TITLE, fileName)
                        type = "application/text"
                    }
                    startActivityForResult(intent, REQUEST_CODE_LOG)
                }
                .create()
            d.show()
            // maximize dialog size, because log lines are long
            d.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)

            true
        }
        if (!prefs.getBoolean(Prefs.TEMP_LOGGER, false))
            findPreference<Preference>("read_log")?.isVisible = false

        findPreference<Preference>("debug")?.isVisible = BuildConfig.DEBUG

        findPreference<Preference>("debug.quests")?.setOnPreferenceClickListener {
            startActivity(Intent(context, ShowQuestFormsActivity::class.java))
            true
        }

        findPreference<Preference>("debug.links")?.setOnPreferenceClickListener {
            startActivity(Intent(context, ShowLinksActivity::class.java))
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbarTitleAndIcon(view.findViewById(R.id.toolbar))
    }

    override fun onStart() {
        super.onStart()

        setQuestPreferenceSummary()
        setQuestPresetsPreferenceSummary()

        visibleQuestTypeSource.addListener(visibleQuestTypeListener)
        questPresetsSource.addListener(questPresetsListener)
    }

    override fun onStop() {
        super.onStop()

        visibleQuestTypeSource.removeListener(visibleQuestTypeListener)
        questPresetsSource.removeListener(questPresetsListener)
    }

    override fun onResume() {
        super.onResume()
        prefs.addListener(Prefs.AUTOSYNC, onAutosyncChanged)
        prefs.addListener(Prefs.THEME_SELECT, onThemeChanged)
        prefs.addListener(Prefs.LANGUAGE_SELECT, onLanguageChanged)
        prefs.addListener(Prefs.RESURVEY_INTERVALS, onResurveyIntervalsChanged)
        prefs.addListener(Prefs.EXPERT_MODE, onExpertModeChanged)
    }

    override fun onPause() {
        super.onPause()
        prefs.removeListener(Prefs.AUTOSYNC, onAutosyncChanged)
        prefs.removeListener(Prefs.THEME_SELECT, onThemeChanged)
        prefs.removeListener(Prefs.LANGUAGE_SELECT, onLanguageChanged)
        prefs.removeListener(Prefs.RESURVEY_INTERVALS, onResurveyIntervalsChanged)
        prefs.removeListener(Prefs.EXPERT_MODE, onExpertModeChanged)
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

    // todo: remove
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK || data == null || requestCode != REQUEST_CODE_LOG)
            return
        val uri = data.data ?: return
        activity?.contentResolver?.openOutputStream(uri)?.use { os ->
            os.bufferedWriter().use { it.write(TempLogger.getLog().joinToString("\n")) }
        }
    }

    private suspend fun deleteCache() = withContext(Dispatchers.IO) {
        downloadedTilesController.clear()
        mapDataController.clear()
        questTypeRegistry.forEach { it.deleteMetadataOlderThan(nowAsEpochMilliseconds()) }
        noteController.clear()
    }

    private suspend fun deleteTiles() = withContext(Dispatchers.IO) {
        context?.externalCacheDirs?.forEach { it.purge() }
    }
    private suspend fun unhideQuests() = withContext(Dispatchers.IO) {
        osmQuestController.unhideAll() + osmNoteQuestController.unhideAll() + externalSourceQuestController.unhideAll()
    }

    private fun setQuestPreferenceSummary() {
        val enabledCount = questTypeRegistry.count { visibleQuestTypeSource.isVisible(it) }
        val totalCount = questTypeRegistry.size
        val summary = getString(R.string.pref_subtitle_quests, enabledCount, totalCount)
        viewLifecycleScope.launch {
            findPreference<Preference>("quests")?.summary = summary
        }
    }

    private fun setQuestPresetsPreferenceSummary() {
        val presetName = questPresetsSource.selectedQuestPresetName ?: getString(R.string.quest_presets_default_name)
        val summary = getString(R.string.pref_subtitle_quests_preset_name, presetName)
        viewLifecycleScope.launch {
            findPreference<Preference>("quest_presets")?.summary = summary
        }
    }

}

private const val REQUEST_CODE_LOG = 9743143
