package de.westnordost.streetcomplete.screens.settings

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.Context
import android.os.Build
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestController
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.preferences.ResurveyIntervalsUpdater
import de.westnordost.streetcomplete.data.visiblequests.DayNightQuestFilter
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderController
import de.westnordost.streetcomplete.data.visiblequests.VisibleEditTypeController
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.common.dialogs.SimpleListPickerDialog
import de.westnordost.streetcomplete.ui.common.settings.Preference
import de.westnordost.streetcomplete.ui.common.settings.SwitchPreference
import de.westnordost.streetcomplete.util.dialogs.setViewWithDefaultPadding
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.ktx.getActivity
import de.westnordost.streetcomplete.util.ktx.hasPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun QuestSettingsScreen(
    onClickBack: () -> Unit,
) {
    val ctx = LocalContext.current
    val prefs: Preferences = koinInject()
    val scope = rememberCoroutineScope()
    val visibleEditTypeController: VisibleEditTypeController = koinInject()
    val dayNightQuestFilter: DayNightQuestFilter = koinInject()
    val questTypeOrderController: QuestTypeOrderController = koinInject()
    val resurveyIntervalsUpdater: ResurveyIntervalsUpdater = koinInject()
    var showDayNightDialog by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.pref_screen_quests)) },
            windowInsets = AppBarDefaults.topAppBarWindowInsets,
            navigationIcon = { IconButton(onClick = onClickBack) { BackIcon() } },
        )
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                    )
                )
        ) {
            Preference(
                name = stringResource(R.string.pref_day_night_title),
                onClick = { showDayNightDialog = true },
            ) {
                Text(stringResource(Prefs.DayNightBehavior.valueOf(prefs.getString(Prefs.DAY_NIGHT_BEHAVIOR, "IGNORE")).titleResId))
            }
            if (prefs.expertMode)
                Preference(
                    name = stringResource(R.string.advanced_resurvey_title),
                    onClick = { advancedResurveyDialog(prefs, ctx, resurveyIntervalsUpdater) },
                    description = stringResource(R.string.pref_advanced_resurvey_summary)
                )
            if (prefs.expertMode)
                SwitchPreference(
                    name = stringResource(R.string.pref_quest_settings_preset_title),
                    description = stringResource(R.string.pref_quest_settings_preset_summary),
                    pref = Prefs.QUEST_SETTINGS_PER_PRESET,
                    default = false,
                    onCheckedChange = { OsmQuestController.reloadQuestTypes() },
                )
            if (prefs.expertMode)
                SwitchPreference(
                    name = stringResource(R.string.pref_dynamic_quest_creation_title),
                    description = stringResource(R.string.pref_dynamic_quest_creation_summary),
                    pref = Prefs.DYNAMIC_QUEST_CREATION,
                    default = false,
                    onCheckedChange = { scope.launch(Dispatchers.IO) { visibleEditTypeController.onVisibilitiesChanged() } }
                )
            Preference(
                name = stringResource(R.string.pref_quest_monitor_title),
                onClick = { questMonitorDialog(prefs, ctx) },
                description = stringResource(R.string.pref_quest_monitor_summary)
            )
            SwitchPreference(
                name = stringResource(R.string.pref_hide_overlay_quests),
                pref = Prefs.HIDE_OVERLAY_QUESTS,
                default = true,
                onCheckedChange = { scope.launch(Dispatchers.IO) { visibleEditTypeController.onVisibilitiesChanged() } }
            )
        }
    }
    if (showDayNightDialog)
        SimpleListPickerDialog(
            onDismissRequest = { showDayNightDialog = false },
            items = Prefs.DayNightBehavior.entries,
            onItemSelected = {
                prefs.putString(Prefs.DAY_NIGHT_BEHAVIOR, it.name)
                scope.launch(Dispatchers.IO) {
                    dayNightQuestFilter.reload()
                    visibleEditTypeController.onVisibilitiesChanged()
                    questTypeOrderController.onQuestTypeOrderChanged()
                }
            },
            title = { Text(stringResource(R.string.pref_day_night_title)) },
            selectedItem = Prefs.DayNightBehavior.valueOf(prefs.getString(Prefs.DAY_NIGHT_BEHAVIOR, "IGNORE")),
            getItemName = { stringResource(it.titleResId) }
        )
}

// todo: composable
private fun advancedResurveyDialog(prefs: Preferences, context: Context, resurveyIntervalsUpdater: ResurveyIntervalsUpdater) {
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

    AlertDialog.Builder(context)
        .setTitle(R.string.advanced_resurvey_title)
        .setViewWithDefaultPadding(layout)
        .setNegativeButton(android.R.string.cancel, null)
        .setPositiveButton(android.R.string.ok) { _, _ ->
            prefs.putString(Prefs.RESURVEY_DATE, dateEditText.text.toString())
            prefs.putString(Prefs.RESURVEY_KEYS, keyEditText.text.toString())
            resurveyIntervalsUpdater.update()
        }
        .show()
}

// todo: composable
private fun questMonitorDialog(prefs: Preferences, context: Context) {
    val layout = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    val enable = SwitchCompat(context).apply {
        isChecked = prefs.getBoolean(Prefs.QUEST_MONITOR, false)
        setText(R.string.pref_quest_monitor_title)
        setOnCheckedChangeListener { _, b ->
            val activity = context.getActivity() ?: return@setOnCheckedChangeListener
            if (!b) return@setOnCheckedChangeListener
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !activity.hasPermission(ACCESS_FINE_LOCATION)) {
                isChecked = false
                ActivityCompat.requestPermissions(activity, arrayOf(ACCESS_FINE_LOCATION), 0)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !activity.hasPermission(ACCESS_BACKGROUND_LOCATION))  {
                isChecked = false
                ActivityCompat.requestPermissions(activity, arrayOf(ACCESS_BACKGROUND_LOCATION), 0)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !activity.hasPermission(POST_NOTIFICATIONS)) {
                isChecked = false
                ActivityCompat.requestPermissions(activity, arrayOf(POST_NOTIFICATIONS), 0)
            }
        }
    }
    val downloadSwitch = SwitchCompat(context).apply {
        isChecked = prefs.getBoolean(Prefs.QUEST_MONITOR_DOWNLOAD, false)
        setText(R.string.pref_quest_monitor_download)
        setPadding(0, 0, 0, context.resources.dpToPx(8).toInt())
    }
    val activeText = TextView(context).apply { setText(R.string.quest_monitor_active_request) }
    val gpsSwitch = SwitchCompat(context).apply {
        isChecked = prefs.getBoolean(Prefs.QUEST_MONITOR_GPS, false)
        setText(R.string.quest_monitor_gps)
    }
    val netSwitch = SwitchCompat(context).apply {
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

    AlertDialog.Builder(context)
        .setTitle(R.string.pref_quest_monitor_title)
        .setViewWithDefaultPadding(ScrollView(context).apply { addView(layout) })
        .setNegativeButton(android.R.string.cancel, null)
        .setPositiveButton(android.R.string.ok) { _, _ ->
            prefs.putBoolean(Prefs.QUEST_MONITOR, enable.isChecked)
            prefs.putBoolean(Prefs.QUEST_MONITOR_GPS, gpsSwitch.isChecked)
            prefs.putBoolean(Prefs.QUEST_MONITOR_NET, netSwitch.isChecked)
            prefs.putBoolean(Prefs.QUEST_MONITOR_DOWNLOAD, downloadSwitch.isChecked)
            prefs.prefs.putFloat(Prefs.QUEST_MONITOR_RADIUS, accuracyEditText.text.toString().toFloatOrNull() ?: 50f)
        }
        .show()
}
