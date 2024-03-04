package de.westnordost.streetcomplete.screens.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import de.westnordost.streetcomplete.ApplicationConstants.DELETE_OLD_DATA_AFTER
import de.westnordost.streetcomplete.ApplicationConstants.REFRESH_DATA_AFTER
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesController
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestHidden
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestsHiddenController
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestsHiddenSource
import de.westnordost.streetcomplete.data.osmnotes.NoteController
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestHidden
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestsHiddenController
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestsHiddenSource
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
import de.westnordost.streetcomplete.util.getDefaultTheme
import de.westnordost.streetcomplete.util.getSelectedLocales
import de.westnordost.streetcomplete.util.ktx.format
import de.westnordost.streetcomplete.util.ktx.getYamlObject
import de.westnordost.streetcomplete.util.ktx.purge
import de.westnordost.streetcomplete.util.ktx.setUpToolbarTitleAndIcon
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.prefs.Preferences
import de.westnordost.streetcomplete.util.setDefaultLocales
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.util.Locale

/** Shows the settings lists */
class SettingsFragment : TwoPaneListFragment(), HasTitle {

    private val prefs: Preferences by inject()
    private val downloadedTilesController: DownloadedTilesController by inject()
    private val noteController: NoteController by inject()
    private val mapDataController: MapDataController by inject()
    private val osmQuestsHiddenController: OsmQuestsHiddenController by inject()
    private val osmNoteQuestsHiddenController: OsmNoteQuestsHiddenController by inject()
    private val resurveyIntervalsUpdater: ResurveyIntervalsUpdater by inject()
    private val questTypeRegistry: QuestTypeRegistry by inject()
    private val visibleQuestTypeSource: VisibleQuestTypeSource by inject()
    private val questPresetsSource: QuestPresetsSource by inject()

    override val title: String get() = getString(R.string.action_settings)

    private val visibleQuestTypeListener = object : VisibleQuestTypeSource.Listener {
        override fun onQuestTypeVisibilityChanged(questType: QuestType, visible: Boolean) { setQuestPreferenceSummary() }
        override fun onQuestTypeVisibilitiesChanged() { setQuestPreferenceSummary() }
    }

    private val questPresetsListener = object : QuestPresetsSource.Listener {
        override fun onSelectedQuestPresetChanged() { setQuestPresetsPreferenceSummary() }
        override fun onAddedQuestPreset(preset: QuestPreset) { setQuestPresetsPreferenceSummary() }
        override fun onRenamedQuestPreset(preset: QuestPreset) { setQuestPresetsPreferenceSummary() }
        override fun onDeletedQuestPreset(presetId: Long) { setQuestPresetsPreferenceSummary() }
    }

    private val osmQuestsHiddenListener = object : OsmQuestsHiddenSource.Listener {
        override fun onHid(edit: OsmQuestHidden) { setHiddenQuestsSummary() }
        override fun onUnhid(edit: OsmQuestHidden) { setHiddenQuestsSummary() }
        override fun onUnhidAll() { setHiddenQuestsSummary() }
    }

    private val osmNoteQuestsHiddenListener = object : OsmNoteQuestsHiddenSource.Listener {
        override fun onHid(edit: OsmNoteQuestHidden) { setHiddenQuestsSummary() }
        override fun onUnhid(edit: OsmNoteQuestHidden) { setHiddenQuestsSummary() }
        override fun onUnhidAll() { setHiddenQuestsSummary() }
    }

    private val onAutosyncChanged = {
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
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        PreferenceManager.setDefaultValues(requireContext(), R.xml.preferences, false)
        addPreferencesFromResource(R.xml.preferences)

        findPreference<NumberPickerPreference>("map.tilecache")?.setSummaryProvider { pref ->
            requireContext().getString(R.string.pref_tilecache_size_summary, (pref as NumberPickerPreference).value)
        }

        findPreference<Preference>("delete_cache")?.setOnPreferenceClickListener {
            val dialogBinding = DialogDeleteCacheBinding.inflate(layoutInflater)
            dialogBinding.descriptionText.text = resources.getString(R.string.delete_cache_dialog_message,
                (1.0 * REFRESH_DATA_AFTER / (24 * 60 * 60 * 1000)).format(Locale.getDefault(), 1),
                (1.0 * DELETE_OLD_DATA_AFTER / (24 * 60 * 60 * 1000)).format(Locale.getDefault(), 1)
            )
            AlertDialog.Builder(requireContext())
                .setView(dialogBinding.root)
                .setPositiveButton(R.string.delete_confirmation) { _, _ -> lifecycleScope.launch { deleteCache() } }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
            true
        }

        findPreference<Preference>("quests.restore.hidden")?.setOnPreferenceClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.restore_dialog_message)
                .setPositiveButton(R.string.restore_confirmation) { _, _ ->
                    onHiddenQuestRestore()
                }
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

        buildLanguageSelector()
    }

    private fun onHiddenQuestRestore() {
        lifecycleScope.launch {
            val hidden = unhideQuests()
            context?.toast(
                getString(R.string.restore_hidden_success, hidden),
                Toast.LENGTH_LONG
            )
        }
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

        setHiddenQuestsSummary()
        setQuestPreferenceSummary()
        setQuestPresetsPreferenceSummary()

        visibleQuestTypeSource.addListener(visibleQuestTypeListener)
        questPresetsSource.addListener(questPresetsListener)
        osmNoteQuestsHiddenController.addListener(osmNoteQuestsHiddenListener)
        osmQuestsHiddenController.addListener(osmQuestsHiddenListener)
    }

    override fun onStop() {
        super.onStop()

        visibleQuestTypeSource.removeListener(visibleQuestTypeListener)
        questPresetsSource.removeListener(questPresetsListener)
        osmNoteQuestsHiddenController.removeListener(osmNoteQuestsHiddenListener)
        osmQuestsHiddenController.removeListener(osmQuestsHiddenListener)
    }

    override fun onResume() {
        super.onResume()
        prefs.addListener(Prefs.AUTOSYNC, onAutosyncChanged)
        prefs.addListener(Prefs.THEME_SELECT, onThemeChanged)
        prefs.addListener(Prefs.LANGUAGE_SELECT, onLanguageChanged)
        prefs.addListener(Prefs.RESURVEY_INTERVALS, onResurveyIntervalsChanged)
    }

    override fun onPause() {
        super.onPause()
        prefs.removeListener(Prefs.AUTOSYNC, onAutosyncChanged)
        prefs.removeListener(Prefs.THEME_SELECT, onThemeChanged)
        prefs.removeListener(Prefs.LANGUAGE_SELECT, onLanguageChanged)
        prefs.removeListener(Prefs.RESURVEY_INTERVALS, onResurveyIntervalsChanged)
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
        context?.externalCacheDir?.purge()
        downloadedTilesController.clear()
        mapDataController.clear()
        noteController.clear()
    }

    private suspend fun unhideQuests() = withContext(Dispatchers.IO) {
        osmQuestsHiddenController.unhideAll() + osmNoteQuestsHiddenController.unhideAll()
    }

    private fun countHiddenQuests(): Long =
        osmQuestsHiddenController.countAll() + osmNoteQuestsHiddenController.countAll()

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

    private fun setHiddenQuestsSummary() {
        viewLifecycleScope.launch {
            val amountOfHiddenQuests = withContext(Dispatchers.IO) { countHiddenQuests() }
            val pref = findPreference<Preference>("quests.restore.hidden")
            pref?.summary = requireContext().getString(R.string.pref_title_quests_restore_hidden_summary, amountOfHiddenQuests)
            pref?.isEnabled = amountOfHiddenQuests > 0
        }
    }
}
