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
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import androidx.preference.TwoStatePreference
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SettingsListener
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.ApplicationConstants.DELETE_OLD_DATA_AFTER_DAYS
import de.westnordost.streetcomplete.ApplicationConstants.REFRESH_DATA_AFTER
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.DialogDeleteCacheBinding
import de.westnordost.streetcomplete.screens.HasTitle
import de.westnordost.streetcomplete.screens.TwoPaneListFragment
import de.westnordost.streetcomplete.screens.settings.debug.ShowQuestFormsActivity
import de.westnordost.streetcomplete.util.TempLogger
import de.westnordost.streetcomplete.util.dialogs.setDefaultDialogPadding
import de.westnordost.streetcomplete.util.ktx.format
import de.westnordost.streetcomplete.util.ktx.observe
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.ktx.setUpToolbarTitleAndIcon
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

/** Shows the settings lists */
class SettingsFragment : TwoPaneListFragment(), HasTitle {

    private val viewModel by viewModel<SettingsViewModel>()

    private val prefs: ObservableSettings by inject()

    override val title: String get() = getString(R.string.action_settings)

    private val listeners = mutableListOf<SettingsListener>()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        PreferenceManager.setDefaultValues(requireContext(), R.xml.preferences, false)
        addPreferencesFromResource(R.xml.preferences)

        findPreference<Preference>("delete_cache")?.setOnPreferenceClickListener {
            val dialogBinding = DialogDeleteCacheBinding.inflate(layoutInflater)
            dialogBinding.descriptionText.text = resources.getString(R.string.delete_cache_dialog_message2,
                (1.0 * REFRESH_DATA_AFTER / (24 * 60 * 60 * 1000)).format(Locale.getDefault(), 1),
                (1.0 * prefs.getInt(Prefs.DATA_RETAIN_TIME, DELETE_OLD_DATA_AFTER_DAYS)).format(Locale.getDefault(), 1)
            )
            AlertDialog.Builder(requireContext())
                .setView(dialogBinding.root)
                .setNeutralButton(R.string.delete_confirmation_both) { _, _ ->
                    viewModel.deleteTiles()
                    viewModel.deleteCache()
                }
                .setPositiveButton(R.string.delete_confirmation_tiles) { _, _ -> viewModel.deleteTiles() }
                .setNegativeButton(R.string.delete_confirmation_data) { _, _ -> viewModel.deleteCache() }
                .show()
            true
        }

        findPreference<Preference>("quests.restore.hidden")?.setOnPreferenceClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.restore_dialog_message)
                .setMessage(R.string.restore_dialog_hint)
                .setPositiveButton(R.string.restore_confirmation) { _, _ -> viewModel.unhideQuests() }
                .setNegativeButton(android.R.string.cancel, null)
                .show()

            true
        }

        findPreference<TwoStatePreference>(Prefs.EXPERT_MODE)?.setOnPreferenceChangeListener { _, any ->
            if (any != false)
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.general_warning)
                    .setMessage(R.string.pref_expert_mode_message)
                    .setPositiveButton(R.string.dialog_button_understood, null)
                    .setNegativeButton(android.R.string.cancel) { d, _ -> d.cancel() }
                    .setOnCancelListener { findPreference<SwitchPreference>(Prefs.EXPERT_MODE)?.isChecked = false }
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
                setDefaultDialogPadding() // not a dialog, but still suitable
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
                        val fileName = "${ApplicationConstants.NAME}_${BuildConfig.VERSION_NAME}_log_${nowAsEpochMilliseconds()}.txt"
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

    // todo: remove
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK || data == null || requestCode != REQUEST_CODE_LOG)
            return
        val uri = data.data ?: return
        activity?.contentResolver?.openOutputStream(uri)?.use { os ->
            os.bufferedWriter().use { it.write(TempLogger.getLog().joinToString("\n")) }
        }
    }

}

private const val REQUEST_CODE_LOG = 9743143
