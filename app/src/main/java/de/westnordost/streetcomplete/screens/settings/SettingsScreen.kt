package de.westnordost.streetcomplete.screens.settings

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ApplicationConstants.DELETE_OLD_DATA_AFTER
import de.westnordost.streetcomplete.ApplicationConstants.REFRESH_DATA_AFTER
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.preferences.Autosync
import de.westnordost.streetcomplete.data.preferences.ResurveyIntervals
import de.westnordost.streetcomplete.data.preferences.Theme
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.common.NextScreenIcon
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmationDialog
import de.westnordost.streetcomplete.ui.common.dialogs.InfoDialog
import de.westnordost.streetcomplete.ui.common.dialogs.SimpleListPickerDialog
import de.westnordost.streetcomplete.ui.common.settings.Preference
import de.westnordost.streetcomplete.ui.common.settings.PreferenceCategory
import de.westnordost.streetcomplete.util.ktx.format
import java.util.Locale

/** Shows the settings lists */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onClickShowQuestForms: () -> Unit,
    onClickPresetSelection: () -> Unit,
    onClickQuestSelection: () -> Unit,
    onClickBack: () -> Unit,
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

    var showDeleteCacheConfirmation by remember { mutableStateOf(false) }
    var showRestoreHiddenQuestsConfirmation by remember { mutableStateOf(false) }
    var showUploadTutorialInfo by remember { mutableStateOf(false) }

    var showThemeSelect by remember { mutableStateOf(false) }
    var showLanguageSelect by remember { mutableStateOf(false) }
    var showAutosyncSelect by remember { mutableStateOf(false) }
    var showResurveyIntervalsSelect by remember { mutableStateOf(false) }

    val presetNameOrDefault = selectedPresetName ?: stringResource(R.string.quest_presets_default_name)

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

    if (showDeleteCacheConfirmation) {
        ConfirmationDialog(
            onDismissRequest = { showDeleteCacheConfirmation = false },
            onConfirmed = { viewModel.deleteCache() },
            text = {
                val locale = Locale.getDefault()
                Text(stringResource(
                    R.string.delete_cache_dialog_message,
                    (1.0 * REFRESH_DATA_AFTER / (24 * 60 * 60 * 1000)).format(locale, 1),
                    (1.0 * DELETE_OLD_DATA_AFTER / (24 * 60 * 60 * 1000)).format(locale, 1)
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
    ResurveyIntervals.LESS_OFTEN -> R.string.resurvey_intervals_less_often
    ResurveyIntervals.DEFAULT -> R.string.resurvey_intervals_default
    ResurveyIntervals.MORE_OFTEN -> R.string.resurvey_intervals_more_often
}

private val Theme.titleResId: Int get() = when (this) {
    Theme.LIGHT -> R.string.theme_light
    Theme.DARK -> R.string.theme_dark
    Theme.SYSTEM -> R.string.theme_system_default
}

private fun getLanguageDisplayName(languageTag: String): String? {
    if (languageTag.isEmpty()) return null
    val locale = Locale.forLanguageTag(languageTag)
    return locale.getDisplayName(locale)
}
