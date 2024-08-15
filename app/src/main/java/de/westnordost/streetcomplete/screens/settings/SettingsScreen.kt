package de.westnordost.streetcomplete.screens.settings

import android.content.Context
import android.os.Build
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.ApplicationConstants.REFRESH_DATA_AFTER
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.preferences.Autosync
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.preferences.ResurveyIntervals
import de.westnordost.streetcomplete.data.preferences.Theme
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.common.NextScreenIcon
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmationDialog
import de.westnordost.streetcomplete.ui.common.dialogs.InfoDialog
import de.westnordost.streetcomplete.ui.common.dialogs.SimpleListPickerDialog
import de.westnordost.streetcomplete.ui.common.settings.Preference
import de.westnordost.streetcomplete.ui.common.settings.PreferenceCategory
import de.westnordost.streetcomplete.util.TempLogger
import de.westnordost.streetcomplete.util.dialogs.setDefaultDialogPadding
import de.westnordost.streetcomplete.util.ktx.format
import de.westnordost.streetcomplete.util.logs.DatabaseLogger
import de.westnordost.streetcomplete.util.logs.Log
import org.koin.compose.koinInject
import java.util.Locale

/** Shows the settings lists */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onClickShowQuestForms: () -> Unit,
    onClickPresetSelection: () -> Unit,
    onClickQuestSelection: () -> Unit,
    onClickBack: () -> Unit,
    onClickSceeFragment: (Int) -> Unit,
) {
    val hiddenQuestCount by viewModel.hiddenQuestCount.collectAsState()
    val questTypeCount by viewModel.questTypeCount.collectAsState()
    val selectedPresetName by viewModel.selectedQuestPresetName.collectAsState()
    val selectableLanguageCodes by viewModel.selectableLanguageCodes.collectAsState()

    val resurveyIntervals by viewModel.resurveyIntervals.collectAsState()
    val showAllNotes by viewModel.showAllNotes.collectAsState()
    val autosync by viewModel.autosync.collectAsState()
    val theme by viewModel.theme.collectAsState()
    val keepScreenOn by viewModel.keepScreenOn.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val expertMode by viewModel.expertMode.collectAsState()

    var showDeleteCacheConfirmation by remember { mutableStateOf(false) }
    var showRestoreHiddenQuestsConfirmation by remember { mutableStateOf(false) }
    var showUploadTutorialInfo by remember { mutableStateOf(false) }

    var showThemeSelect by remember { mutableStateOf(false) }
    var showLanguageSelect by remember { mutableStateOf(false) }
    var showAutosyncSelect by remember { mutableStateOf(false) }
    var showResurveyIntervalsSelect by remember { mutableStateOf(false) }
    var showExpertModeConfirmation by remember { mutableStateOf(false) }

    val presetNameOrDefault = selectedPresetName ?: stringResource(R.string.quest_presets_default_name)

    val c = LocalContext.current
    val databaseLogger: DatabaseLogger = koinInject()
    val prefs: Preferences = koinInject()
    var useDebugLogger by remember { mutableStateOf(prefs.getBoolean(Prefs.TEMP_LOGGER, false)) }

    fun useDebugLogger(use: Boolean, prefs: Preferences, databaseLogger: DatabaseLogger) {
        prefs.putBoolean(Prefs.TEMP_LOGGER, use)
        useDebugLogger = use
        if (use) {
            Log.instances.removeAll { it is DatabaseLogger }
            Log.instances.add(TempLogger)
        } else {
            Log.instances.remove(TempLogger)
            Log.instances.add(databaseLogger)
        }
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.action_settings)) },
            navigationIcon = { IconButton(onClick = onClickBack) { BackIcon() } },
        )
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            PreferenceCategory(stringResource(R.string.pref_category_quests)) {

                Preference(
                    name = stringResource(R.string.action_manage_presets),
                    onClick = onClickPresetSelection,
                    description = stringResource(R.string.action_manage_presets_summary)
                ) {
                    Text(presetNameOrDefault)
                    NextScreenIcon()
                }

                Preference(
                    name = stringResource(R.string.pref_title_quests2),
                    onClick = onClickQuestSelection,
                    description = questTypeCount?.let {
                        stringResource(R.string.pref_subtitle_quests, it.enabled, it.total)
                    }
                ) { NextScreenIcon() }

                Preference(
                    name = stringResource(R.string.pref_title_resurvey_intervals),
                    onClick = { showResurveyIntervalsSelect = true },
                    description = stringResource(R.string.pref_title_resurvey_intervals_summary)
                ) {
                    Text(stringResource(resurveyIntervals.titleResId))
                }

                Preference(
                    name = stringResource(R.string.pref_title_show_notes_not_phrased_as_questions),
                    onClick = { viewModel.setShowAllNotes(!showAllNotes) },
                    description = stringResource(
                        if (showAllNotes) R.string.pref_summaryOn_show_notes_not_phrased_as_questions
                        else R.string.pref_summaryOff_show_notes_not_phrased_as_questions
                    )
                ) {
                    Switch(
                        checked = showAllNotes,
                        onCheckedChange = { viewModel.setShowAllNotes(it) }
                    )
                }
            }

            PreferenceCategory(stringResource(R.string.pref_category_communication)) {
                Preference(
                    name = stringResource(R.string.pref_title_sync2),
                    onClick = { showAutosyncSelect = true }
                ) {
                    Text(stringResource(autosync.titleResId))
                }
            }

            PreferenceCategory(stringResource(R.string.pref_category_display)) {

                Preference(
                    name = stringResource(R.string.pref_title_language_select2),
                    onClick = { showLanguageSelect = true },
                ) {
                    Text(
                        selectedLanguage?.let { getLanguageDisplayName(it) }
                            ?: stringResource(R.string.language_default)
                    )
                }

                Preference(
                    name = stringResource(R.string.pref_title_theme_select),
                    onClick = { showThemeSelect = true },
                ) {
                    Text(stringResource(theme.titleResId))
                }

                Preference(
                    name = stringResource(R.string.pref_title_keep_screen_on),
                    onClick = { viewModel.setKeepScreenOn(!keepScreenOn) },
                ) {
                    Switch(
                        checked = keepScreenOn,
                        onCheckedChange = { viewModel.setKeepScreenOn(it) }
                    )
                }
            }

            PreferenceCategory(stringResource(R.string.pref_category_advanced)) {

                Preference(
                    name = stringResource(R.string.pref_title_delete_cache),
                    onClick = { showDeleteCacheConfirmation = true },
                    description = stringResource(R.string.pref_title_delete_cache_summary)
                )

                Preference(
                    name = stringResource(R.string.pref_title_quests_restore_hidden),
                    onClick = { showRestoreHiddenQuestsConfirmation = true },
                    description = stringResource(R.string.pref_title_quests_restore_hidden_summary, hiddenQuestCount)
                )
            }

            PreferenceCategory(stringResource(R.string.pref_category_mods)) {

                Preference(
                    name = stringResource(R.string.pref_expert_mode_title),
                    onClick = {
                        if (expertMode) viewModel.setExpertMode(false)
                        else showExpertModeConfirmation = true
                    },
                    description = stringResource(R.string.pref_expert_mode_summary)
                ) {
                    Switch(
                        checked = expertMode,
                        onCheckedChange = {
                            if (!it) viewModel.setExpertMode(it)
                            else showExpertModeConfirmation = true
                        }
                    )
                }

                Preference(
                    name = stringResource(R.string.pref_screen_ui),
                    onClick = { onClickSceeFragment(1) },
                )

                Preference(
                    name = stringResource(R.string.pref_screen_display),
                    onClick = { onClickSceeFragment(2) },
                )

                Preference(
                    name = stringResource(R.string.pref_screen_quests),
                    onClick = { onClickSceeFragment(3) },
                )

                Preference(
                    name = stringResource(R.string.pref_screen_notes),
                    onClick = { onClickSceeFragment(4) },
                )

                Preference(
                    name = stringResource(R.string.pref_screen_data_management),
                    onClick = { onClickSceeFragment(5) },
                )

                if (BuildConfig.DEBUG) {
                    Preference(
                        name = "Debug log reader",
                        onClick = { showOldLogReader(c) }
                    )

                    Preference(
                        name = "Use temp debug logger",
                        onClick = { useDebugLogger(!useDebugLogger, prefs, databaseLogger) },
                    ) {
                        Switch(
                            checked = useDebugLogger,
                            onCheckedChange = { useDebugLogger(it, prefs, databaseLogger) }
                        )
                    }
                }
            }

            if (BuildConfig.DEBUG) {
                PreferenceCategory("Debug") {
                    Preference(
                        name = "Show Quest Forms",
                        onClick = onClickShowQuestForms
                    ) { NextScreenIcon() }
                }
            }
        }
    }

    if (showExpertModeConfirmation) {
        ConfirmationDialog(
            onDismissRequest = { showExpertModeConfirmation = false },
            onConfirmed = { viewModel.setExpertMode(true) },
            text = { Text(stringResource(R.string.pref_expert_mode_message)) },
            confirmButtonText = stringResource(R.string.dialog_button_understood)
        )
    }
    if (showDeleteCacheConfirmation) {
        ConfirmationDialog(
            onDismissRequest = { showDeleteCacheConfirmation = false },
            onConfirmed = { viewModel.deleteCache() },
            text = {
                val locale = Locale.getDefault()
                Text(stringResource(
                    R.string.delete_cache_dialog_message,
                    (1.0 * REFRESH_DATA_AFTER / (24 * 60 * 60 * 1000)).format(locale, 1),
                    (1.0 * viewModel.prefs.getInt(Prefs.DATA_RETAIN_TIME, ApplicationConstants.DELETE_OLD_DATA_AFTER_DAYS)).format(locale, 1)
                ))
            },
            confirmButtonText = stringResource(R.string.delete_confirmation)
        )
    }
    if (showRestoreHiddenQuestsConfirmation) {
        ConfirmationDialog(
            onDismissRequest = { showRestoreHiddenQuestsConfirmation = false },
            onConfirmed = { viewModel.unhideQuests() },
            title = { Text(stringResource(R.string.restore_dialog_message)) },
            text = { Text(stringResource(R.string.restore_dialog_hint)) },
            confirmButtonText = stringResource(R.string.restore_confirmation)
        )
    }
    if (showUploadTutorialInfo) {
        InfoDialog(
            onDismissRequest = { showUploadTutorialInfo = false },
            text = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(painterResource(R.drawable.ic_file_upload_48dp), null)
                    Text(stringResource(R.string.dialog_tutorial_upload))
                }
            },
        )
    }
    if (showThemeSelect) {
        SimpleListPickerDialog(
            onDismissRequest = { showThemeSelect = false },
            items = Theme.entries,
            onItemSelected = { viewModel.setTheme(it) },
            title = { Text(stringResource(R.string.pref_title_theme_select)) },
            selectedItem = theme,
            getItemName = { stringResource(it.titleResId) }
        )
    }
    if (showAutosyncSelect) {
        SimpleListPickerDialog(
            onDismissRequest = { showAutosyncSelect = false },
            items = Autosync.entries,
            onItemSelected = {
                viewModel.setAutosync(it)
                if (it != Autosync.ON) {
                    showUploadTutorialInfo = true
                }
            },
            title = { Text(stringResource(R.string.pref_title_sync2)) },
            selectedItem = autosync,
            getItemName = { stringResource(it.titleResId) }
        )
    }
    if (showResurveyIntervalsSelect) {
        SimpleListPickerDialog(
            onDismissRequest = { showResurveyIntervalsSelect = false },
            items = ResurveyIntervals.entries,
            onItemSelected = { viewModel.setResurveyIntervals(it) },
            title = { Text(stringResource(R.string.pref_title_resurvey_intervals)) },
            selectedItem = resurveyIntervals,
            getItemName = { stringResource(it.titleResId) }
        )
    }
    val codes = selectableLanguageCodes
    if (showLanguageSelect && codes != null) {
        val namesByCode = remember(codes) { codes.associateWith { getLanguageDisplayName(it) } }
        val sortedCodes = listOf(null) + codes.sortedBy { namesByCode[it]?.lowercase() }
        SimpleListPickerDialog(
            onDismissRequest = { showLanguageSelect = false },
            items = sortedCodes,
            onItemSelected = { viewModel.setSelectedLanguage(it) },
            title = { Text(stringResource(R.string.pref_title_language_select2)) },
            selectedItem = selectedLanguage,
            getItemName = { item ->
                item?.let { getLanguageDisplayName(it) }
                    ?: stringResource(R.string.language_default)
            }
        )
    }
}

private val Autosync.titleResId: Int get() = when (this) {
    Autosync.ON -> R.string.autosync_on
    Autosync.WIFI -> R.string.autosync_only_on_wifi
    Autosync.OFF -> R.string.autosync_off
}

private val ResurveyIntervals.titleResId: Int get() = when (this) {
    ResurveyIntervals.EVEN_LESS_OFTEN -> R.string.resurvey_intervals_even_less_often
    ResurveyIntervals.LESS_OFTEN -> R.string.resurvey_intervals_less_often
    ResurveyIntervals.DEFAULT -> R.string.resurvey_intervals_default
    ResurveyIntervals.MORE_OFTEN -> R.string.resurvey_intervals_more_often
}

private val Theme.titleResId: Int get() = when (this) {
    Theme.LIGHT -> R.string.theme_light
    Theme.DARK -> R.string.theme_dark
    Theme.SYSTEM -> R.string.theme_system_default
    Theme.DARK_CONTRAST -> R.string.theme_dark_contrast
}

private fun getLanguageDisplayName(languageTag: String): String? {
    if (languageTag.isEmpty()) return null
    val locale = Locale.forLanguageTag(languageTag)
    return locale.getDisplayName(locale)
}

private fun showOldLogReader(context: Context) { // todo: repeats lines... is it the logger, or the dialog?
    var reversed = false
    var filter = ""
    var maxLines = 200
    val log = TextView(context)
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
    val scrollLog = ScrollView(context).apply {
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
    val reverseButton = Button(context)
    reverseButton.setText(R.string.pref_read_reverse_button)
    reverseButton.setOnClickListener {
        reversed = !reversed
        reloadText()
        scrollLog.scrollY = 0
    }
    val filterView = EditText(context).apply {
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
    val layout = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    layout.addView(LinearLayout(context).apply {
        addView(reverseButton)
        addView(filterView)
    }) // put this on top, or layout will need more work to keep this visible
    layout.addView(scrollLog)
    val d = AlertDialog.Builder(context)
        .setTitle(R.string.pref_read_log_title)
        .setView(layout) // not using default padding to allow longer log lines (looks ugly, but is very convenient)
        .setPositiveButton(R.string.close, null)
        .create()
    d.show()
    // maximize dialog size, because log lines are long
    d.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
}
