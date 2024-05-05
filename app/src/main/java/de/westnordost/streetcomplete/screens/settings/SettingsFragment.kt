package de.westnordost.streetcomplete.screens.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.russhwolf.settings.SettingsListener
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.ApplicationConstants.DELETE_OLD_DATA_AFTER
import de.westnordost.streetcomplete.ApplicationConstants.REFRESH_DATA_AFTER
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.DialogDeleteCacheBinding
import de.westnordost.streetcomplete.screens.HasTitle
import de.westnordost.streetcomplete.screens.TwoPaneListFragment
import de.westnordost.streetcomplete.screens.settings.debug.ShowQuestFormsActivity
import de.westnordost.streetcomplete.util.ktx.format
import de.westnordost.streetcomplete.util.ktx.observe
import de.westnordost.streetcomplete.util.ktx.setUpToolbarTitleAndIcon
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

/** Shows the settings lists */
class SettingsFragment : TwoPaneListFragment(), HasTitle {

    private val viewModel by viewModel<SettingsViewModel>()

    override val title: String get() = getString(R.string.action_settings)

    private val listeners = mutableListOf<SettingsListener>()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        PreferenceManager.setDefaultValues(requireContext(), R.xml.preferences, false)
        addPreferencesFromResource(R.xml.preferences)

        findPreference<Preference>("delete_cache")?.setOnPreferenceClickListener {
            val dialogBinding = DialogDeleteCacheBinding.inflate(layoutInflater)
            dialogBinding.descriptionText.text = resources.getString(R.string.delete_cache_dialog_message,
                (1.0 * REFRESH_DATA_AFTER / (24 * 60 * 60 * 1000)).format(Locale.getDefault(), 1),
                (1.0 * DELETE_OLD_DATA_AFTER / (24 * 60 * 60 * 1000)).format(Locale.getDefault(), 1)
            )
            AlertDialog.Builder(requireContext())
                .setView(dialogBinding.root)
                .setPositiveButton(R.string.delete_confirmation) { _, _ -> viewModel.deleteCache() }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
            true
        }

        findPreference<Preference>("quests.restore.hidden")?.setOnPreferenceClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.restore_dialog_message)
                .setPositiveButton(R.string.restore_confirmation) { _, _ -> viewModel.unhideQuests() }
                .setNegativeButton(android.R.string.cancel, null)
                .show()

            true
        }

        findPreference<Preference>("debug")?.isVisible = BuildConfig.DEBUG

        findPreference<Preference>("debug.quests")?.setOnPreferenceClickListener {
            startActivity(Intent(context, ShowQuestFormsActivity::class.java))
            true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpToolbarTitleAndIcon(view.findViewById(R.id.toolbar))

        observe(viewModel.hiddenQuestCount) { count ->
            val pref = findPreference<Preference>("quests.restore.hidden")
            pref?.summary = requireContext().getString(R.string.pref_title_quests_restore_hidden_summary, count)
            pref?.isEnabled = count > 0
        }

        observe(viewModel.selectedQuestPresetName) { presetName ->
            val presetDisplayName = presetName ?: getString(R.string.quest_presets_default_name)
            findPreference<Preference>("quest_presets")?.summary =
                getString(R.string.pref_subtitle_quests_preset_name, presetDisplayName)
        }

        observe(viewModel.questTypeCount) { count ->
            if (count == null) return@observe
            findPreference<Preference>("quests")?.summary =
                getString(R.string.pref_subtitle_quests, count.enabled, count.total)
        }

        observe(viewModel.selectableLanguageCodes) { languageCodes ->
            if (languageCodes == null) return@observe
            val entryValues = languageCodes.toMutableList()
            val entries = entryValues.map {
                val locale = Locale.forLanguageTag(it)
                val name = locale.displayName
                val nativeName = locale.getDisplayName(locale)
                return@map nativeName + if (name != nativeName) " — $name" else ""
            }.toMutableList()

            // add default as first element
            entryValues.add(0, "")
            entries.add(0, getString(R.string.language_default))

            val pref = findPreference<ListPreference>(Prefs.LANGUAGE_SELECT)
            pref?.entries = entries.toTypedArray()
            pref?.entryValues = entryValues.toTypedArray()
            // must set this (default) so that the preference updates its summary ... 🙄
            pref?.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
        }

        observe(viewModel.tileCacheSize) { size ->
            findPreference<Preference>(Prefs.MAP_TILECACHE_IN_MB)?.summary =
                getString(R.string.pref_tilecache_size_summary, size)
        }

        listeners += viewModel.prefs.addStringOrNullListener(Prefs.AUTOSYNC) { autosync ->
            val autosyncOrDefault = Prefs.Autosync.valueOf(autosync ?: ApplicationConstants.DEFAULT_AUTOSYNC)
            if (autosyncOrDefault != Prefs.Autosync.ON) {
                AlertDialog.Builder(requireContext())
                    .setView(layoutInflater.inflate(R.layout.dialog_tutorial_upload, null))
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            }
        }

        listeners += viewModel.prefs.addStringOrNullListener(Prefs.THEME_SELECT) {
            activity?.let { ActivityCompat.recreate(it) }
        }

        listeners += viewModel.prefs.addStringOrNullListener(Prefs.LANGUAGE_SELECT) {
            activity?.let { ActivityCompat.recreate(it) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listeners.forEach { it.deactivate() }
        listeners.clear()
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
}
