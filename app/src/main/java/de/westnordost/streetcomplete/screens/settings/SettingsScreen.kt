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
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.ApplicationConstants.DELETE_OLD_DATA_AFTER
import de.westnordost.streetcomplete.ApplicationConstants.REFRESH_DATA_AFTER
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.common.settings.PreferenceCategory
import de.westnordost.streetcomplete.ui.common.settings.Preference
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.common.NextScreenIcon
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmationDialog
import de.westnordost.streetcomplete.ui.common.dialogs.InfoDialog
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
    val selectedPresetName by viewModel.selectedQuestPresetName.collectAsState()
    val tileCacheSize by viewModel.tileCacheSize.collectAsState()

    var showDeleteCacheDialog by remember { mutableStateOf(false) }
    var showRestoreHiddenQuestsDialog by remember { mutableStateOf(false) }
    var showUploadTutorialDialog by remember { mutableStateOf(false) }

    // TODO showuploadtutorialdialog if Prefs.AUTOSYNC != ON

    val presetNameOrDefault = selectedPresetName ?: stringResource(R.string.quest_presets_default_name)

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.action_settings)) },
            navigationIcon = { IconButton(onClick = onClickBack) { BackIcon() } },
        )
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            PreferenceCategory(stringResource(R.string.pref_category_quests)) {

                Preference(
                    name = stringResource(R.string.action_manage_presets2),
                    onClick = onClickPresetSelection,
                    description = stringResource(R.string.action_manage_presets_summary)
                ) {
                    Text(presetNameOrDefault)
                    NextScreenIcon()
                }

                Preference(
                    name = stringResource(R.string.pref_title_quests2),
                    onClick = onClickQuestSelection,
                    description = stringResource(R.string.pref_subtitle_quests_preset_name, presetNameOrDefault)
                ) { NextScreenIcon() }

                Preference(
                    name = stringResource(R.string.pref_title_resurvey_intervals),
                    onClick = { /*TODO*/ },
                    description = stringResource(R.string.pref_title_resurvey_intervals_summary)
                ) {
                    Text(stringResource(viewModel.prefs.getResurveyIntervals().titleResId))
                }

                Preference(
                    name = stringResource(R.string.pref_title_show_notes_not_phrased_as_questions),
                    onClick = { /*TODO switch*/ },
                    description = stringResource(
                        if (viewModel.prefs.getBoolean(Prefs.SHOW_NOTES_NOT_PHRASED_AS_QUESTIONS, false)) R.string.pref_summaryOn_show_notes_not_phrased_as_questions
                        else R.string.pref_summaryOff_show_notes_not_phrased_as_questions
                    )
                ) {
                    Switch(
                        checked = viewModel.prefs.getBoolean(Prefs.SHOW_NOTES_NOT_PHRASED_AS_QUESTIONS, false),
                        onCheckedChange =  { /*TODO switch*/ }
                    )
                }
            }

            PreferenceCategory(stringResource(R.string.pref_category_communication)) {
                Preference(
                    name = stringResource(R.string.pref_title_sync2),
                    onClick = { /*TODO select */ }
                ) {
                    Text(stringResource(viewModel.prefs.getAutosync().titleResId))
                }
            }

            PreferenceCategory(stringResource(R.string.pref_category_display)) {

                Preference(
                    name = stringResource(R.string.pref_title_keep_screen_on),
                    onClick = { /*TODO switch*/ },
                ) {
                    Switch(
                        checked = viewModel.prefs.getBoolean(Prefs.KEEP_SCREEN_ON, false),
                        onCheckedChange = { /*TODO switch*/ }
                    )
                }

                Preference(
                    name = stringResource(R.string.pref_title_theme_select),
                    onClick = { /*TODO theme select*/ },
                ) {
                    Text(stringResource(viewModel.prefs.getTheme().titleResId))
                }

                Preference(
                    name = stringResource(R.string.pref_title_language_select2),
                    onClick = { /*TODO language select (better as own screen with search) */ },
                ) {
                    Text(getLanguageDisplayName(viewModel.prefs.getString(Prefs.LANGUAGE_SELECT, "")))
                }
            }

            PreferenceCategory(stringResource(R.string.pref_category_advanced)) {

                Preference(
                    name = stringResource(R.string.pref_title_delete_cache),
                    onClick = { showDeleteCacheDialog = true },
                    description = stringResource(R.string.pref_title_delete_cache_summary)
                )
                // TODO disable if no hidden quests
                Preference(
                    name = stringResource(R.string.pref_title_quests_restore_hidden),
                    onClick = { showRestoreHiddenQuestsDialog = true },
                    description = stringResource(R.string.pref_title_quests_restore_hidden_summary, hiddenQuestCount)
                )

                Preference(
                    name = stringResource(R.string.pref_title_map_cache),
                    onClick = { /* TODO cache size select (slider or number select, 10-250, standard 50) */ }
                ) {
                    Text(stringResource(R.string.pref_tilecache_size_summary, tileCacheSize))
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

    if (showDeleteCacheDialog) {
        ConfirmationDialog(
            onDismissRequest = { showDeleteCacheDialog = false },
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
    if (showRestoreHiddenQuestsDialog) {
        ConfirmationDialog(
            onDismissRequest = { showRestoreHiddenQuestsDialog = false },
            onConfirmed = { viewModel.unhideQuests() },
            title = { Text(stringResource(R.string.restore_dialog_message)) },
            confirmButtonText = stringResource(R.string.restore_confirmation)
        )
    }
    if (showUploadTutorialDialog) {
        InfoDialog(
            onDismissRequest = { showUploadTutorialDialog = false },
            text = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(painterResource(R.drawable.ic_file_upload_48dp), null)
                    Text(stringResource(R.string.dialog_tutorial_upload))
                }
            },
        )
    }
}

@Composable
@ReadOnlyComposable
private fun getLanguageDisplayName(languageTag: String): String {
    if (languageTag.isEmpty()) return stringResource(R.string.language_default)
    val locale = Locale.forLanguageTag(languageTag)
    val name = locale.displayName
    val nativeName = locale.getDisplayName(locale)
    return nativeName + if (name != nativeName) " — $name" else ""
}

private val Prefs.Autosync.titleResId: Int get() = when (this) {
    Prefs.Autosync.ON -> R.string.autosync_on
    Prefs.Autosync.WIFI -> R.string.autosync_only_on_wifi
    Prefs.Autosync.OFF -> R.string.autosync_off
}

private val Prefs.ResurveyIntervals.titleResId: Int get() = when (this) {
    Prefs.ResurveyIntervals.LESS_OFTEN -> R.string.resurvey_intervals_less_often
    Prefs.ResurveyIntervals.DEFAULT -> R.string.resurvey_intervals_default
    Prefs.ResurveyIntervals.MORE_OFTEN -> R.string.resurvey_intervals_more_often
}

private val Prefs.Theme.titleResId: Int get() = when (this) {
    Prefs.Theme.LIGHT -> R.string.theme_light
    Prefs.Theme.DARK -> R.string.theme_dark
    Prefs.Theme.SYSTEM -> R.string.theme_system_default
}

// TODO move

private fun ObservableSettings.getTheme(): Prefs.Theme {
    // disregard removed deprecated setting (-> treat as default, i.e. follow system setting)
    // as of June 2024, 95% of active installs from google play use an Android where AUTO is deprecated
    val theme = getStringOrNull(Prefs.THEME_SELECT).takeUnless { it == "AUTO" }
    return theme?.let { Prefs.Theme.valueOf(it) } ?: Prefs.Theme.SYSTEM
}

private fun ObservableSettings.getAutosync(): Prefs.Autosync =
    getStringOrNull(Prefs.AUTOSYNC)?.let { Prefs.Autosync.valueOf(it) } ?: Prefs.Autosync.ON

private fun ObservableSettings.getResurveyIntervals(): Prefs.ResurveyIntervals =
    getStringOrNull(Prefs.RESURVEY_INTERVALS)?.let { Prefs.ResurveyIntervals.valueOf(it) }
        ?: Prefs.ResurveyIntervals.DEFAULT
