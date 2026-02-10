package de.westnordost.streetcomplete.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AppBarDefaults
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
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ApplicationConstants.DELETE_OLD_DATA_AFTER
import de.westnordost.streetcomplete.ApplicationConstants.REFRESH_DATA_AFTER
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.data.preferences.Autosync
import de.westnordost.streetcomplete.data.preferences.ResurveyIntervals
import de.westnordost.streetcomplete.data.preferences.Theme
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.action_manage_presets
import de.westnordost.streetcomplete.resources.action_manage_presets_summary
import de.westnordost.streetcomplete.resources.action_settings
import de.westnordost.streetcomplete.resources.autosync_off
import de.westnordost.streetcomplete.resources.autosync_on
import de.westnordost.streetcomplete.resources.autosync_only_on_wifi
import de.westnordost.streetcomplete.resources.delete_cache_dialog_message
import de.westnordost.streetcomplete.resources.delete_confirmation
import de.westnordost.streetcomplete.resources.dialog_tutorial_upload
import de.westnordost.streetcomplete.resources.ic_file_upload_48
import de.westnordost.streetcomplete.resources.language_default
import de.westnordost.streetcomplete.resources.pref_category_advanced
import de.westnordost.streetcomplete.resources.pref_category_communication
import de.westnordost.streetcomplete.resources.pref_category_display
import de.westnordost.streetcomplete.resources.pref_category_quests
import de.westnordost.streetcomplete.resources.pref_subtitle_quests
import de.westnordost.streetcomplete.resources.pref_summaryOff_show_notes_not_phrased_as_questions
import de.westnordost.streetcomplete.resources.pref_summaryOn_show_notes_not_phrased_as_questions
import de.westnordost.streetcomplete.resources.pref_title_delete_cache
import de.westnordost.streetcomplete.resources.pref_title_delete_cache_summary
import de.westnordost.streetcomplete.resources.pref_title_keep_screen_on
import de.westnordost.streetcomplete.resources.pref_title_language_select2
import de.westnordost.streetcomplete.resources.pref_title_messages
import de.westnordost.streetcomplete.resources.pref_title_overlays
import de.westnordost.streetcomplete.resources.pref_title_quests2
import de.westnordost.streetcomplete.resources.pref_title_quests_restore_hidden
import de.westnordost.streetcomplete.resources.pref_title_quests_restore_hidden_summary
import de.westnordost.streetcomplete.resources.pref_title_resurvey_intervals
import de.westnordost.streetcomplete.resources.pref_title_resurvey_intervals_summary
import de.westnordost.streetcomplete.resources.pref_title_show_notes_not_phrased_as_questions
import de.westnordost.streetcomplete.resources.pref_title_sync2
import de.westnordost.streetcomplete.resources.pref_title_theme_select
import de.westnordost.streetcomplete.resources.pref_title_zoom_buttons
import de.westnordost.streetcomplete.resources.quest_presets_default_name
import de.westnordost.streetcomplete.resources.restore_confirmation
import de.westnordost.streetcomplete.resources.restore_dialog_message
import de.westnordost.streetcomplete.resources.resurvey_intervals_default
import de.westnordost.streetcomplete.resources.resurvey_intervals_less_often
import de.westnordost.streetcomplete.resources.resurvey_intervals_more_often
import de.westnordost.streetcomplete.resources.theme_dark
import de.westnordost.streetcomplete.resources.theme_light
import de.westnordost.streetcomplete.resources.theme_system_default
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.common.NextScreenIcon
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmationDialog
import de.westnordost.streetcomplete.ui.common.dialogs.InfoDialog
import de.westnordost.streetcomplete.ui.common.settings.Preference
import de.westnordost.streetcomplete.ui.common.settings.PreferenceCategory
import de.westnordost.streetcomplete.ui.common.settings.Select
import de.westnordost.streetcomplete.util.ktx.getDisplayName
import de.westnordost.streetcomplete.util.locale.NumberFormatter
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Shows the settings lists */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onClickShowQuestForms: () -> Unit,
    onClickPresetSelection: () -> Unit,
    onClickQuestSelection: () -> Unit,
    onClickOverlaySelection: () -> Unit,
    onClickLanguageSelection: () -> Unit,
    onClickMessagesSelection: () -> Unit,
    onClickBack: () -> Unit,
) {
    val hiddenQuestCount by viewModel.hiddenQuestCount.collectAsState()
    val questTypeCount by viewModel.questTypeCount.collectAsState()
    val overlayCount by viewModel.overlayCount.collectAsState()
    val selectedPresetName by viewModel.selectedEditTypePresetName.collectAsState()

    val resurveyIntervals by viewModel.resurveyIntervals.collectAsState()
    val showAllNotes by viewModel.showAllNotes.collectAsState()
    val autosync by viewModel.autosync.collectAsState()
    val theme by viewModel.theme.collectAsState()
    val keepScreenOn by viewModel.keepScreenOn.collectAsState()
    val showZoomButtons by viewModel.showZoomButtons.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()

    var showDeleteCacheConfirmation by remember { mutableStateOf(false) }
    var showRestoreHiddenQuestsConfirmation by remember { mutableStateOf(false) }
    var showUploadTutorialInfo by remember { mutableStateOf(false) }

    var showThemeSelect by remember { mutableStateOf(false) }
    var showAutosyncSelect by remember { mutableStateOf(false) }
    var showResurveyIntervalsSelect by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(Res.string.action_settings)) },
            windowInsets = AppBarDefaults.topAppBarWindowInsets,
            navigationIcon = { IconButton(onClick = onClickBack) { BackIcon() } },
        )
        Column(Modifier
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.safeDrawing.only(
                WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
            ))
        ) {
            PreferenceCategory(stringResource(Res.string.pref_category_quests)) {

                Preference(
                    name = stringResource(Res.string.action_manage_presets),
                    onClick = onClickPresetSelection,
                    description = stringResource(Res.string.action_manage_presets_summary)
                ) {
                    Text(
                        text = selectedPresetName ?: stringResource(Res.string.quest_presets_default_name),
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    NextScreenIcon()
                }

                Preference(
                    name = stringResource(Res.string.pref_title_quests2),
                    onClick = onClickQuestSelection,
                    description = questTypeCount?.let {
                        stringResource(Res.string.pref_subtitle_quests, it.enabled, it.total)
                    }
                ) { NextScreenIcon() }

                Preference(
                    name = stringResource(Res.string.pref_title_overlays),
                    onClick = onClickOverlaySelection,
                    description = overlayCount?.let {
                        stringResource(Res.string.pref_subtitle_quests, it.enabled, it.total)
                    }
                ) { NextScreenIcon() }

                Preference(
                    name = stringResource(Res.string.pref_title_resurvey_intervals),
                    onClick = { showResurveyIntervalsSelect = true },
                    description = stringResource(Res.string.pref_title_resurvey_intervals_summary)
                ) {
                    Select(
                        items = ResurveyIntervals.entries,
                        selectedItem = resurveyIntervals,
                        onSelected = { viewModel.setResurveyIntervals(it) },
                        expanded = showResurveyIntervalsSelect,
                        onDismissRequest = { showResurveyIntervalsSelect = false }
                    ) {
                        Text(stringResource(it.title))
                    }
                }

                Preference(
                    name = stringResource(Res.string.pref_title_show_notes_not_phrased_as_questions),
                    onClick = { viewModel.setShowAllNotes(!showAllNotes) },
                    description = stringResource(
                        if (showAllNotes) Res.string.pref_summaryOn_show_notes_not_phrased_as_questions
                        else Res.string.pref_summaryOff_show_notes_not_phrased_as_questions
                    )
                ) {
                    Switch(checked = showAllNotes, onCheckedChange = null)
                }
            }

            PreferenceCategory(stringResource(Res.string.pref_category_communication)) {
                Preference(
                    name = stringResource(Res.string.pref_title_sync2),
                    onClick = { showAutosyncSelect = true }
                ) {
                    Select(
                        items = Autosync.entries,
                        selectedItem = autosync,
                        onSelected = {
                            viewModel.setAutosync(it)
                            if (it != Autosync.ON) {
                                showUploadTutorialInfo = true
                            }
                        },
                        expanded = showAutosyncSelect,
                        onDismissRequest = { showAutosyncSelect = false },
                    ) { Text(stringResource(it.title)) }
                }
            }

            PreferenceCategory(stringResource(Res.string.pref_category_display)) {

                Preference(
                    name = stringResource(Res.string.pref_title_language_select2),
                    onClick = onClickLanguageSelection,
                ) {
                    Text(
                        text = selectedLanguage?.let { getLanguageDisplayName(it) }
                            ?: stringResource(Res.string.language_default),
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    NextScreenIcon()
                }

                Preference(
                    name = stringResource(Res.string.pref_title_theme_select),
                    onClick = { showThemeSelect = true },
                ) {
                    Select(
                        items = Theme.entries,
                        selectedItem = theme,
                        onSelected = { viewModel.setTheme(it) },
                        expanded = showThemeSelect,
                        onDismissRequest = { showThemeSelect = false }
                    ) { Text(stringResource(it.title)) }
                }

                Preference(
                    name = stringResource(Res.string.pref_title_messages),
                    onClick = onClickMessagesSelection,
                ) { NextScreenIcon() }

                Preference(
                    name = stringResource(Res.string.pref_title_zoom_buttons),
                    onClick = { viewModel.setShowZoomButtons(!showZoomButtons) },
                ) {
                    Switch(checked = showZoomButtons, onCheckedChange = null)
                }

                Preference(
                    name = stringResource(Res.string.pref_title_keep_screen_on),
                    onClick = { viewModel.setKeepScreenOn(!keepScreenOn) },
                ) {
                    Switch(checked = keepScreenOn, onCheckedChange = null)
                }
            }

            PreferenceCategory(stringResource(Res.string.pref_category_advanced)) {

                Preference(
                    name = stringResource(Res.string.pref_title_delete_cache),
                    onClick = { showDeleteCacheConfirmation = true },
                    description = stringResource(Res.string.pref_title_delete_cache_summary)
                )

                Preference(
                    name = stringResource(Res.string.pref_title_quests_restore_hidden),
                    onClick = { showRestoreHiddenQuestsConfirmation = true },
                    description = stringResource(Res.string.pref_title_quests_restore_hidden_summary, hiddenQuestCount)
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
                val numberFormatter = NumberFormatter(Locale.current, maxFractionDigits = 1)
                Text(stringResource(
                    Res.string.delete_cache_dialog_message,
                    numberFormatter.format(1.0 * REFRESH_DATA_AFTER / (24 * 60 * 60 * 1000)),
                    numberFormatter.format(1.0 * DELETE_OLD_DATA_AFTER / (24 * 60 * 60 * 1000))
                ))
            },
            confirmButtonText = stringResource(Res.string.delete_confirmation)
        )
    }
    if (showRestoreHiddenQuestsConfirmation) {
        ConfirmationDialog(
            onDismissRequest = { showRestoreHiddenQuestsConfirmation = false },
            onConfirmed = { viewModel.unhideQuests() },
            title = { Text(stringResource(Res.string.restore_dialog_message)) },
            confirmButtonText = stringResource(Res.string.restore_confirmation)
        )
    }
    if (showUploadTutorialInfo) {
        InfoDialog(
            onDismissRequest = { showUploadTutorialInfo = false },
            text = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(painterResource(Res.drawable.ic_file_upload_48), null)
                    Text(stringResource(Res.string.dialog_tutorial_upload))
                }
            },
        )
    }
}

private val Autosync.title: StringResource get() = when (this) {
    Autosync.ON -> Res.string.autosync_on
    Autosync.WIFI -> Res.string.autosync_only_on_wifi
    Autosync.OFF -> Res.string.autosync_off
}

private val ResurveyIntervals.title: StringResource get() = when (this) {
    ResurveyIntervals.LESS_OFTEN -> Res.string.resurvey_intervals_less_often
    ResurveyIntervals.DEFAULT -> Res.string.resurvey_intervals_default
    ResurveyIntervals.MORE_OFTEN -> Res.string.resurvey_intervals_more_often
}

private val Theme.title: StringResource get() = when (this) {
    Theme.LIGHT -> Res.string.theme_light
    Theme.DARK -> Res.string.theme_dark
    Theme.SYSTEM -> Res.string.theme_system_default
}

private fun getLanguageDisplayName(languageTag: String): String? {
    if (languageTag.isEmpty()) return null
    val locale = Locale(languageTag)
    return locale.getDisplayName(locale) ?: languageTag
}
