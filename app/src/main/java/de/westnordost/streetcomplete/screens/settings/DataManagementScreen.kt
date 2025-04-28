package de.westnordost.streetcomplete.screens.settings

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.core.content.edit
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.StreetCompleteApplication
import de.westnordost.streetcomplete.data.Cleaner
import de.westnordost.streetcomplete.data.ConflictAlgorithm
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuestController
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuestTables
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestsHiddenTable
import de.westnordost.streetcomplete.data.osmnotes.notequests.NoteQuestsHiddenTable
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.presets.EditTypePreset
import de.westnordost.streetcomplete.data.presets.EditTypePresetsController
import de.westnordost.streetcomplete.data.presets.EditTypePresetsTable
import de.westnordost.streetcomplete.data.urlconfig.UrlConfigController
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderTable
import de.westnordost.streetcomplete.data.visiblequests.VisibleEditTypeController
import de.westnordost.streetcomplete.data.visiblequests.VisibleEditTypeTable
import de.westnordost.streetcomplete.overlays.custom.getCustomOverlayIndices
import de.westnordost.streetcomplete.overlays.custom.getIndexedCustomOverlayPref
import de.westnordost.streetcomplete.quests.amenity_cover.AddAmenityCover
import de.westnordost.streetcomplete.quests.custom.CustomQuest
import de.westnordost.streetcomplete.quests.osmose.OsmoseDao
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.common.dialogs.SimpleListPickerDialog
import de.westnordost.streetcomplete.ui.common.dialogs.TextInputDialog
import de.westnordost.streetcomplete.ui.common.settings.Preference
import de.westnordost.streetcomplete.ui.common.settings.SwitchPreference
import de.westnordost.streetcomplete.util.dialogs.setViewWithDefaultPadding
import de.westnordost.streetcomplete.util.getFakeCustomOverlays
import de.westnordost.streetcomplete.util.ktx.getActivity
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.compose.koinInject
import java.io.BufferedWriter

// todo: there is still a lot of non-compose in here, but that's for later...
@Composable
fun DataManagementScreen(
    onClickBack: () -> Unit,
) {
    val prefs: Preferences = koinInject()
    val cleaner: Cleaner = koinInject()
    val db: Database = koinInject()
    val editTypePresetsController: EditTypePresetsController = koinInject()
    val urlConfigController: UrlConfigController = koinInject()
    val visibleEditTypeController: VisibleEditTypeController = koinInject()
    val osmoseDao: OsmoseDao = koinInject()
    val externalSourceQuestController: ExternalSourceQuestController = koinInject()
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var showDeleteAfterDialog by remember { mutableStateOf(false) }
    var showGpsIntervalDialog by remember { mutableStateOf(false) }
    var showNetIntervalDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var currentSetting by rememberSaveable { mutableStateOf("") }
    val exportPicker = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode != Activity.RESULT_OK || it.data == null)
            return@rememberLauncherForActivityResult
        val uri = it.data?.data ?: return@rememberLauncherForActivityResult
        val activity = ctx.getActivity() ?: return@rememberLauncherForActivityResult
        when (currentSetting) {
            "settings" -> exportSettings(uri, activity)
            "hidden_quests" -> exportHidden(uri, activity, db)
            "presets" -> exportPresets(uri, activity, db, editTypePresetsController, urlConfigController)
            "overlays" -> exportOverlays(uri, activity, prefs)
        }
        currentSetting = ""
    }
    val importPicker = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode != Activity.RESULT_OK || it.data == null)
            return@rememberLauncherForActivityResult
        val uri = it.data?.data ?: return@rememberLauncherForActivityResult
        val activity = ctx.getActivity() ?: return@rememberLauncherForActivityResult
        when (currentSetting) {
            "settings" -> if (!importSettings(uri, activity, osmoseDao, externalSourceQuestController))
                ctx.toast(ctx.getString(R.string.import_error), Toast.LENGTH_LONG)
            "hidden_quests" -> importHidden(uri, activity, db, visibleEditTypeController)
            "presets" -> importPresets(uri, activity, db, visibleEditTypeController)
            "overlays" -> importOverlays(uri, activity)
        }
        currentSetting = ""
    }
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.pref_screen_data_management)) },
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
            SwitchPreference(
                name = stringResource(R.string.pref_auto_download_title),
                description = stringResource(R.string.pref_auto_download_summary),
                pref = Prefs.AUTO_DOWNLOAD,
                default = true,
            )
            SwitchPreference(
                name = stringResource(R.string.pref_manual_download_cache_title),
                description = stringResource(R.string.pref_manual_download_cache_summary),
                pref = Prefs.MANUAL_DOWNLOAD_OVERRIDE_CACHE,
                default = true,
            )
            Preference(
                name = stringResource(R.string.pref_tile_source_title),
                onClick = { showRasterUrlDialog(ctx, StreetCompleteApplication.preferences) },
            )
            Preference(
                name = stringResource(R.string.pref_delete_old_data_after),
                onClick = { showDeleteAfterDialog = true },
                description = stringResource(R.string.pref_delete_old_data_after_summary, prefs.getInt(Prefs.DATA_RETAIN_TIME, 14))
            )
            SwitchPreference(
                name = stringResource(R.string.pref_update_local_statistics),
                description = stringResource(R.string.pref_update_local_statistics_summary),
                pref = Prefs.UPDATE_LOCAL_STATISTICS,
                default = true,
            )
            Preference(
                name = stringResource(R.string.pref_gps_interval_title),
                onClick = { showGpsIntervalDialog = true },
                description = stringResource(R.string.pref_interval_summary, prefs.getInt(Prefs.DATA_RETAIN_TIME, 0))
            )
            Preference(
                name = stringResource(R.string.pref_network_interval_title),
                onClick = { showNetIntervalDialog = true },
                description = stringResource(R.string.pref_interval_summary, prefs.getInt(Prefs.DATA_RETAIN_TIME, 5))
            )
            Preference(
                name = stringResource(R.string.pref_export),
                onClick = { showExportDialog = true },
            )
            Preference(
                name = stringResource(R.string.pref_import),
                onClick = { showImportDialog = true },
            )
        }
        if (showDeleteAfterDialog)
            TextInputDialog(
                onDismissRequest = { showDeleteAfterDialog = false },
                onConfirmed = {
                    prefs.putInt(Prefs.DATA_RETAIN_TIME, it.toIntOrNull() ?: 14)
                    scope.launch(Dispatchers.IO) { cleaner.cleanOld() }
                },
                text = prefs.getInt(Prefs.DATA_RETAIN_TIME, 14).toString(),
                title = { Text(stringResource(R.string.pref_delete_old_data_after_message)) },
                keyboardType = KeyboardType.Number,
                checkTextValid = {
                    val value = it.toIntOrNull()
                    value != null && value >= 3
                }
            )
        if (showGpsIntervalDialog)
            TextInputDialog(
                onDismissRequest = { showGpsIntervalDialog = false },
                onConfirmed = { prefs.putInt(Prefs.GPS_INTERVAL, it.toIntOrNull() ?: 0) },
                text = prefs.getInt(Prefs.GPS_INTERVAL, 0).toString(),
                title = { Text(stringResource(R.string.pref_interval_message)) },
                keyboardType = KeyboardType.Number,
                checkTextValid = {
                    val value = it.toIntOrNull()
                    value != null && value >= 0
                }
            )
        if (showNetIntervalDialog)
            TextInputDialog(
                onDismissRequest = { showNetIntervalDialog = false },
                onConfirmed = { prefs.putInt(Prefs.NETWORK_INTERVAL, it.toIntOrNull() ?: 5) },
                text = prefs.getInt(Prefs.NETWORK_INTERVAL, 5).toString(),
                title = { Text(stringResource(R.string.pref_interval_message)) },
                keyboardType = KeyboardType.Number,
                checkTextValid = {
                    val value = it.toIntOrNull()
                    value != null && value >= 0
                }
            )
        if (showExportDialog)
            SimpleListPickerDialog(
                onDismissRequest = { showExportDialog = false },
                showButtons = false,
                items = listOf("hidden_quests", "presets","overlays","settings"),
                getItemName = {
                    val id = when (it) {
                        "settings" -> R.string.import_export_settings
                        "hidden_quests" -> R.string.import_export_hidden_quests
                        "presets" -> R.string.import_export_presets
                        "overlays" -> R.string.import_export_custom_overlays
                        else -> 0
                    }
                    stringResource(id)
                },
                onItemSelected = {
                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        putExtra(Intent.EXTRA_TITLE, "$it.txt")
                        type = "application/text"
                    }
                    currentSetting = it
                    exportPicker.launch(intent)
                },
                title = { Text(stringResource(R.string.pref_export)) }
            )
        if (showImportDialog)
            SimpleListPickerDialog(
                onDismissRequest = { showImportDialog = false },
                showButtons = false,
                items = listOf("hidden_quests", "presets","overlays","settings"),
                getItemName = {
                    val id = when (it) {
                        "settings" -> R.string.import_export_settings
                        "hidden_quests" -> R.string.import_export_hidden_quests
                        "presets" -> R.string.import_export_presets
                        "overlays" -> R.string.import_export_custom_overlays
                        else -> 0
                    }
                    stringResource(id)
                },
                onItemSelected = {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "*/*" // can't select text file if setting to application/text
                    }
                    currentSetting = it
                    importPicker.launch(intent)
                },
                title = { Text(stringResource(R.string.pref_import)) }
            )
    }
}

private const val BACKUP_HIDDEN_OSM_QUESTS = "quests"
private const val BACKUP_HIDDEN_NOTES = "notes"
private const val BACKUP_HIDDEN_OTHER_QUESTS = "other_source_quests"
private const val BACKUP_PRESETS = "presets"
private const val BACKUP_PRESETS_ORDERS = "orders"
private const val BACKUP_PRESETS_VISIBILITIES = "visibilities"
private const val BACKUP_PRESETS_QUEST_SETTINGS = "quest_settings"

private const val TAG = "DataManagementSettings"

const val LAST_KNOWN_DB_VERSION = 19L

val renamedQuests = mapOf(
    "ExternalQuest" to CustomQuest::class.simpleName!!,
    "AddPicnicTableCover" to AddAmenityCover::class.simpleName!!,
)
fun String.renameUpdatedQuests() =
    renamedQuests.entries.fold(this) { acc, (old, new) -> acc.replace(old, new) }

private fun showRasterUrlDialog(context: Context, prefs: SharedPreferences) {
    var d: AlertDialog? = null
    val currentUrl = prefs.getString(Prefs.RASTER_TILE_URL, ApplicationConstants.RASTER_DEFAULT_URL)!!
    val urlText = EditText(context).apply {
        setText(currentUrl)
        doAfterTextChanged {
            val t = it.toString()
            d?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = t.contains("{x}") && t.contains("{y}") && t.contains("{z}")
        }
    }
    val hideLabelsSwitch = SwitchCompat(context).apply {
        setText(R.string.pref_tile_source_hide_labels)
        isChecked = prefs.getBoolean(Prefs.NO_SATELLITE_LABEL, false)
    }
    val maxZoom = EditText(context).apply {
        inputType = InputType.TYPE_CLASS_NUMBER
        setText(prefs.getInt(Prefs.RASTER_TILE_MAXZOOM, ApplicationConstants.RASTER_DEFAULT_MAXZOOM).toString())
    }
    val layout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        addView(TextView(context).apply { setText(R.string.pref_tile_source_message) })
        addView(urlText)
        addView(TextView(context).apply { setText(R.string.pref_tile_maxzoom) })
        addView(maxZoom)
        addView(hideLabelsSwitch)
    }
    d = AlertDialog.Builder(context)
        .setTitle(R.string.pref_tile_source_title)
        .setViewWithDefaultPadding(layout)
        .setNegativeButton(android.R.string.cancel, null)
        .setNeutralButton(R.string.action_reset) { _, _ ->
            prefs.edit {
                remove(Prefs.RASTER_TILE_URL)
                remove(Prefs.RASTER_TILE_MAXZOOM)
                remove(Prefs.NO_SATELLITE_LABEL)
            }
        }
        .setPositiveButton(android.R.string.ok) { _, _ ->
            prefs.edit {
                putString(Prefs.RASTER_TILE_URL, urlText.text.toString())
                putInt(Prefs.RASTER_TILE_MAXZOOM, maxZoom.text.toString().toInt())
                putBoolean(Prefs.NO_SATELLITE_LABEL, hideLabelsSwitch.isChecked)
            }

            // trigger the listener in MapFragment (if it exists)
            val map = prefs.getString(Prefs.THEME_BACKGROUND, "MAP")
            prefs.edit().putString(Prefs.THEME_BACKGROUND, if (map == "MAP") "AERIAL" else "MAP").apply()
            prefs.edit().putString(Prefs.THEME_BACKGROUND, map).apply()
        }
        .create()
    d.show()
}

private fun exportHidden(uri: Uri, activity: Activity, db: Database) {
    activity.contentResolver?.openOutputStream(uri)?.use { os ->
        val version = db.rawQuery("PRAGMA user_version;") { c -> c.getLong("user_version") }.single()
        if (version > LAST_KNOWN_DB_VERSION)
            activity.toast(activity.getString(R.string.export_warning_db_version), Toast.LENGTH_LONG)

        val hiddenOsmQuests = db.query(OsmQuestsHiddenTable.NAME) { c ->
            c.getLong(OsmQuestsHiddenTable.Columns.ELEMENT_ID).toString() + "," +
                c.getString(OsmQuestsHiddenTable.Columns.ELEMENT_TYPE) + "," +
                c.getString(OsmQuestsHiddenTable.Columns.QUEST_TYPE) + "," +
                c.getLong(OsmQuestsHiddenTable.Columns.TIMESTAMP)
        }
        val hiddenNotes = db.query(NoteQuestsHiddenTable.NAME) { c->
            c.getLong(NoteQuestsHiddenTable.Columns.NOTE_ID).toString() + "," +
                c.getLong(NoteQuestsHiddenTable.Columns.TIMESTAMP)
        }
        val hiddenExternalSourceQuests = db.query(ExternalSourceQuestTables.NAME_HIDDEN) { c ->
            c.getString(ExternalSourceQuestTables.Columns.SOURCE) + "," +
                c.getString(ExternalSourceQuestTables.Columns.ID) + "," +
                c.getLong(ExternalSourceQuestTables.Columns.TIMESTAMP)
        }

        os.bufferedWriter().use {
            it.write(version.toString())
            it.write("\n\n$BACKUP_HIDDEN_OSM_QUESTS\n")
            it.write(hiddenOsmQuests.joinToString("\n"))
            it.write("\n\n$BACKUP_HIDDEN_NOTES\n")
            it.write(hiddenNotes.joinToString("\n"))
            it.write("\n\n$BACKUP_HIDDEN_OTHER_QUESTS\n")
            it.write(hiddenExternalSourceQuests.joinToString("\n") + "\n")
        }
    }
}

private fun exportPresets(uri: Uri, activity: Activity, db: Database, editTypePresetsController: EditTypePresetsController, urlConfigController: UrlConfigController) {
    val allPresets = mutableListOf<EditTypePreset>()
    allPresets.add(EditTypePreset(0, activity.getString(R.string.quest_presets_default_name)))
    allPresets.addAll(editTypePresetsController.getAll())
    val array = allPresets.map { it.name }.toTypedArray()
    val selectedPresets = mutableSetOf<Long>()
    val d = AlertDialog.Builder(activity)
        .setTitle(R.string.import_export_presets_select)
        .setMultiChoiceItems(array, null) { di, which, isChecked ->
            if (isChecked) selectedPresets.add(allPresets[which].id)
            else selectedPresets.remove(allPresets[which].id)
            (di as AlertDialog).getButton(Dialog.BUTTON_POSITIVE)?.isEnabled = selectedPresets.isNotEmpty()
        }
        .setNegativeButton(android.R.string.cancel, null)
        .setPositiveButton(android.R.string.ok) { _, _ ->
            exportPresets(selectedPresets, uri, activity, db, urlConfigController)
        }
        .show()
    d.getButton(Dialog.BUTTON_POSITIVE)?.isEnabled = false
}

private fun exportPresets(ids: Collection<Long>, uri: Uri, activity: Activity, db: Database, urlConfigController: UrlConfigController) {
    activity.contentResolver?.openOutputStream(uri)?.use { os ->
        val version = db.rawQuery("PRAGMA user_version;") { c -> c.getLong("user_version") }.single()
        if (version > LAST_KNOWN_DB_VERSION)
            activity.toast(activity.getString(R.string.export_warning_db_version), Toast.LENGTH_LONG)

        val presetString = ids.joinToString(",")
        val presets = db.query(EditTypePresetsTable.NAME, where = "${EditTypePresetsTable.Columns.EDIT_TYPE_PRESET_ID} IN ($presetString)") { c ->
            c.getLong(EditTypePresetsTable.Columns.EDIT_TYPE_PRESET_ID).toString() + "," +
                c.getString(EditTypePresetsTable.Columns.EDIT_TYPE_PRESET_NAME)
        }.map { "$it,${urlConfigController.create(it.substringBefore(',').toLong())}" }
        val orders = db.query(QuestTypeOrderTable.NAME, where = "${QuestTypeOrderTable.Columns.EDIT_TYPE_PRESET_ID} IN ($presetString)") { c->
            c.getLong(QuestTypeOrderTable.Columns.EDIT_TYPE_PRESET_ID).toString() + "," +
                c.getString(QuestTypeOrderTable.Columns.BEFORE) + "," +
                c.getString(QuestTypeOrderTable.Columns.AFTER)
        }
        val visibilities = db.query(VisibleEditTypeTable.NAME, where = "${VisibleEditTypeTable.Columns.EDIT_TYPE_PRESET_ID} IN ($presetString)") { c ->
            c.getLong(VisibleEditTypeTable.Columns.EDIT_TYPE_PRESET_ID).toString() + "," +
                c.getString(VisibleEditTypeTable.Columns.EDIT_TYPE) + "," +
                c.getLong(VisibleEditTypeTable.Columns.VISIBILITY).toString()
        }
        val perPresetQuestSetting = "\\d+_qs_.+".toRegex()
        val questSettings = StreetCompleteApplication.preferences.all.filterKeys { it.matches(perPresetQuestSetting) && it.substringBefore('_').toLongOrNull() in ids }

        os.bufferedWriter().use {
            it.appendLine(version.toString())
            it.appendLine("\n$BACKUP_PRESETS")
            it.appendLine(presets.joinToString("\n"))
            it.appendLine("\n$BACKUP_PRESETS_ORDERS")
            it.appendLine(orders.joinToString("\n"))
            it.appendLine("\n$BACKUP_PRESETS_VISIBILITIES")
            it.appendLine(visibilities.joinToString("\n"))
            it.appendLine("\n$BACKUP_PRESETS_QUEST_SETTINGS")
            settingsToJsonStream(questSettings, it)
        }
    }
}

// this will ignore settings with value null
@Suppress("UNCHECKED_CAST") // it is checked... but whatever (except string set, because not allowed to check for that)
private fun settingsToJsonStream(settings: Map<String, Any?>, out: BufferedWriter) {
    val booleans = settings.filterValues { it is Boolean } as Map<String, Boolean>
    val ints = settings.filterValues { it is Int } as Map<String, Int>
    val longs = settings.filterValues { it is Long } as Map<String, Long>
    val floats = settings.filterValues { it is Float } as Map<String, Float>
    val strings = settings.filterValues { it is String } as Map<String, String>
    val stringSets = settings.filterValues { it is Set<*> } as Map<String, Set<String>>
    // now write
    out.appendLine("boolean settings")
    out.appendLine( Json.encodeToString(booleans))
    out.appendLine()
    out.appendLine("int settings")
    out.appendLine( Json.encodeToString(ints))
    out.appendLine()
    out.appendLine("long settings")
    out.appendLine( Json.encodeToString(longs))
    out.appendLine()
    out.appendLine("float settings")
    out.appendLine( Json.encodeToString(floats))
    out.appendLine()
    out.appendLine("string settings")
    out.appendLine( Json.encodeToString(strings))
    out.appendLine()
    out.appendLine("string set settings")
    out.appendLine( Json.encodeToString(stringSets))
}

private fun exportOverlays(uri: Uri, activity: Activity, scPrefs: Preferences) {
    val allOverlays = getFakeCustomOverlays(scPrefs, activity.resources, false)
    val array = allOverlays.map { it.changesetComment }.toTypedArray()
    val selectedOverlays = mutableSetOf<String>()
    val d = AlertDialog.Builder(activity)
        .setTitle(R.string.import_export_custom_overlays_select)
        .setMultiChoiceItems(array, null) { di, which, isChecked ->
            if (isChecked) selectedOverlays.add(allOverlays[which].wikiLink!!)
            else selectedOverlays.remove(allOverlays[which].wikiLink!!)
            (di as AlertDialog).getButton(Dialog.BUTTON_POSITIVE)?.isEnabled = selectedOverlays.isNotEmpty()
        }
        .setNegativeButton(android.R.string.cancel, null)
        .setPositiveButton(android.R.string.ok) { _, _ ->
            exportCustomOverlays(selectedOverlays, uri, activity)
        }
        .show()
    d.getButton(Dialog.BUTTON_POSITIVE)?.isEnabled = false
}

private fun exportCustomOverlays(indices: Collection<String>, uri: Uri, activity: Activity) {
    val prefs = StreetCompleteApplication.preferences
    val filterRegex = "custom_overlay_(?:${indices.joinToString("|")})_.*".toRegex()
    val settings = prefs.all.filterKeys { filterRegex.matches(it) }.toMutableMap()
    settings[Prefs.CUSTOM_OVERLAY_INDICES] = indices.joinToString(",")
    if (prefs.getInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, 0).toString() in indices)
        settings[Prefs.CUSTOM_OVERLAY_SELECTED_INDEX] = prefs.getInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, 0)
    activity.contentResolver?.openOutputStream(uri)?.use { it.bufferedWriter().use {
        it.appendLine("overlays")
        settingsToJsonStream(settings, it)
    } }
}

private fun exportSettings(uri: Uri, activity: Activity) {
    val perPresetQuestSetting = "\\d+_qs_.+".toRegex()
    val settings = StreetCompleteApplication.preferences.all.filterKeys {
        !it.contains("TangramPinsSpriteSheet") // this is huge and gets generated if missing anyway
            && !it.contains("TangramIconsSpriteSheet") // this is huge and gets generated if missing anyway
            && it != Preferences.OAUTH2_ACCESS_TOKEN // login
            && !it.contains("osm.") // login data
            && !it.matches(perPresetQuestSetting) // per-preset quest settings should be stored with presets, because preset id is never guaranteed to match
            && !it.startsWith("custom_overlay") // custom overlays are exported separately
    }
    activity.contentResolver?.openOutputStream(uri)?.use { it.bufferedWriter().use { settingsToJsonStream(settings, it) } }
}

private fun importOverlays(uri: Uri, activity: Activity) {
    AlertDialog.Builder(activity)
        .setTitle(R.string.pref_import)
        .setMessage(R.string.import_presets_overlays_message)
        .setPositiveButton(R.string.import_presets_overlays_replace) { _, _ -> if (!importCustomOverlays(uri, true, activity)) activity.toast(activity.getString(R.string.import_error), Toast.LENGTH_LONG) }
        .setNeutralButton(R.string.import_presets_overlays_add) { _, _ -> if (!importCustomOverlays(uri, false, activity)) activity.toast(activity.getString(R.string.import_error), Toast.LENGTH_LONG) }
        .show()
}

private fun importCustomOverlays(uri: Uri, replaceExisting: Boolean, activity: Activity): Boolean {
    val lines = activity.contentResolver?.openInputStream(uri)?.use { it.reader().readLines() } ?: return false
    if (lines.first() != "overlays") return false
    val prefs = StreetCompleteApplication.preferences
    return if (replaceExisting) {
        // first remove old overlays
        // this is necessary because otherwise overlay may remain, but hidden due to not in indices pref
        prefs.edit { prefs.all.keys.forEach { if (it.startsWith("custom_overlay")) remove(it) } }

        val result = readToSettings(lines.subList(1, lines.size))
        // update in case of old data
        if (prefs.contains("custom_overlay_filter") || prefs.contains("custom_overlay_color_key")) {
            val indices = if (prefs.contains(Prefs.CUSTOM_OVERLAY_INDICES)) getCustomOverlayIndices(prefs) else emptyList()
            val newIndex = indices.maxOrNull() ?: 0
            prefs.edit {
                if (prefs.contains("custom_overlay_filter"))
                    putString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_FILTER, newIndex), prefs.getString("custom_overlay_filter", "")!!)
                if (prefs.contains("custom_overlay_color_key"))
                    putString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_COLOR_KEY, newIndex), prefs.getString("custom_overlay_color_key", "")!!)
                remove("custom_overlay_filter")
                remove("custom_overlay_color_key")
                putString(Prefs.CUSTOM_OVERLAY_INDICES, (indices + newIndex).sorted().joinToString(","))
            }
        }
        result
    }
    else {
        val customOverlayRegex = "custom_overlay_(\\d+)_".toRegex()
        val indices = getCustomOverlayIndices(prefs).toMutableSet()
        val offset = indices.maxOrNull()?.let { it + 1 } ?: 0
        val newLines = lines.mapNotNull { line ->
            if (line == "overlays") return@mapNotNull null
            line.replace(customOverlayRegex) { result ->
                if (result.groupValues.size <= 1) throw (IllegalStateException())
                val oldIndex = result.groupValues[1].toInt()
                val newIndex = oldIndex + offset
                indices.add(newIndex)
                "custom_overlay_${newIndex}_"
            }
        }
        val result = readToSettings(newLines)
        prefs.edit {
            // update in case of old data
            if (prefs.contains("custom_overlay_filter") || prefs.contains("custom_overlay_color_key")) {
                val oldOverlayIndex = if (indices.contains(offset)) indices.max() + 1 else offset
                indices.add(oldOverlayIndex)
                if (prefs.contains("custom_overlay_filter"))
                    putString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_FILTER, oldOverlayIndex), prefs.getString("custom_overlay_filter", "")!!)
                if (prefs.contains("custom_overlay_color_key"))
                    putString(getIndexedCustomOverlayPref(Prefs.CUSTOM_OVERLAY_IDX_COLOR_KEY, oldOverlayIndex), prefs.getString("custom_overlay_color_key", "")!!)
                remove("custom_overlay_filter")
                remove("custom_overlay_color_key")
            }
            // set updated indices
            putString(Prefs.CUSTOM_OVERLAY_INDICES, indices.sorted().joinToString(","))
        }
        result
    }
}

private fun readToSettings(list: List<String>): Boolean {
    val i = list.iterator()
    val e = StreetCompleteApplication.preferences.edit()
    try {
        while (i.hasNext()) {
            val next = i.next()
            if (next.isBlank()) continue
            when (next) {
                "boolean settings" -> Json.decodeFromString<Map<String, Boolean>>(i.next()).forEach { e.putBoolean(it.key, it.value) }
                "int settings" -> Json.decodeFromString<Map<String, Int>>(i.next()).forEach { e.putInt(it.key, it.value) }
                "long settings" -> Json.decodeFromString<Map<String, Long>>(i.next()).forEach { e.putLong(it.key, it.value) }
                "float settings" -> Json.decodeFromString<Map<String, Float>>(i.next()).forEach { e.putFloat(it.key, it.value) }
                "string settings" -> Json.decodeFromString<Map<String, String>>(i.next()).forEach { e.putString(it.key, it.value) }
                "string set settings" -> Json.decodeFromString<Map<String, Set<String>>>(i.next()).forEach { e.putStringSet(it.key, it.value) }
            }
        }
        e.apply()
        return true
    } catch (e: Exception) {
        return false
    }
}

private fun importHidden(uri: Uri, activity: Activity, db: Database, visibleEditTypeController: VisibleEditTypeController) {
    // do not delete existing hidden quests; this can be done manually anyway
    val lines = importLinesAndCheck(uri, BACKUP_HIDDEN_OSM_QUESTS, activity, db)

    val quests = mutableListOf<Array<Any?>>()
    val notes = mutableListOf<Array<Any?>>()
    val externalSourceQuests = mutableListOf<Array<Any?>>()
    val added = hashSetOf<String>() // avoid duplicates
    var currentThing = BACKUP_HIDDEN_OSM_QUESTS
    for (line in lines) {
        if (line.isEmpty()) continue
        if (line == BACKUP_HIDDEN_NOTES || line == BACKUP_HIDDEN_OTHER_QUESTS) {
            currentThing = line
            continue
        }
        val split = line.split(",")
        if (split.size < 2) break
        when (currentThing) {
            BACKUP_HIDDEN_OSM_QUESTS -> if (added.add(line)) quests.add(arrayOf(split[0].toLong(), split[1], split[2], split[3].toLong()))
            BACKUP_HIDDEN_NOTES -> if (added.add(line)) notes.add(arrayOf(split[0].toLong(), split[1].toLong()))
            BACKUP_HIDDEN_OTHER_QUESTS -> if (added.add(line)) externalSourceQuests.add(arrayOf(split[0], split[1], split[2].toLong()))
        }
    }

    db.insertMany(OsmQuestsHiddenTable.NAME,
        arrayOf(OsmQuestsHiddenTable.Columns.ELEMENT_ID,
            OsmQuestsHiddenTable.Columns.ELEMENT_TYPE,
            OsmQuestsHiddenTable.Columns.QUEST_TYPE,
            OsmQuestsHiddenTable.Columns.TIMESTAMP),
        quests,
        conflictAlgorithm = ConflictAlgorithm.REPLACE
    )
    db.insertMany(NoteQuestsHiddenTable.NAME,
        arrayOf(NoteQuestsHiddenTable.Columns.NOTE_ID,
            NoteQuestsHiddenTable.Columns.TIMESTAMP),
        notes,
        conflictAlgorithm = ConflictAlgorithm.REPLACE
    )
    db.insertMany(ExternalSourceQuestTables.NAME_HIDDEN,
        arrayOf(ExternalSourceQuestTables.Columns.SOURCE,
            ExternalSourceQuestTables.Columns.ID,
            ExternalSourceQuestTables.Columns.TIMESTAMP),
        externalSourceQuests,
        conflictAlgorithm = ConflictAlgorithm.REPLACE
    )

    // definitely need to reset visible quests
    visibleEditTypeController.onVisibilitiesChanged()
    // imported hidden osmquests are applied, but don't show up in edit history
    // imported other quests are not even applied
}

/** @returns the lines after [checkLine], which is expected to be the second or third line */
private fun importLinesAndCheck(uri: Uri, checkLine: String, activity: Activity, db: Database): List<String> =
    activity.contentResolver?.openInputStream(uri)?.use { it.bufferedReader().use { input ->
        val fileVersion = input.readLine().toLongOrNull()
        if (fileVersion == null || (input.readLine() != checkLine && input.readLine() != checkLine)) {
            Log.w(TAG, "import error, file version $fileVersion, checkLine $checkLine")
            activity.toast(activity.getString(R.string.import_error), Toast.LENGTH_LONG)
            return emptyList()
        }
        val dbVersion = db.rawQuery("PRAGMA user_version;") { c -> c.getLong("user_version") }.single()
        if (fileVersion != dbVersion && (fileVersion > LAST_KNOWN_DB_VERSION || dbVersion > LAST_KNOWN_DB_VERSION)) {
            Log.w(TAG, "import error, file version $fileVersion, dbVersion $dbVersion, last known db version $LAST_KNOWN_DB_VERSION")
            activity.toast(activity.getString(R.string.import_error_db_version), Toast.LENGTH_LONG)
            return emptyList()
        }
        input.readLines().renameUpdatedQuests()
    } } ?: emptyList()

// when importing, names should be updated!
private fun List<String>.renameUpdatedQuests() = map { it.renameUpdatedQuests() }

private fun importPresets(uri: Uri, activity: Activity, db: Database, visibleEditTypeController: VisibleEditTypeController) {
    val lines = importLinesAndCheck(uri, BACKUP_PRESETS, activity, db)
    if (lines.isEmpty()) {
        return
    }
    AlertDialog.Builder(activity)
        .setTitle(R.string.pref_import)
        .setMessage(R.string.import_presets_overlays_message)
        .setPositiveButton(R.string.import_presets_overlays_replace) { _, _ -> importPresets(lines, true, db, visibleEditTypeController) }
        .setNeutralButton(R.string.import_presets_overlays_add) { _, _ -> importPresets(lines, false, db, visibleEditTypeController) }
        .show()
}

private fun importPresets(lines: List<String>, replaceExistingPresets: Boolean, db: Database, visibleEditTypeController: VisibleEditTypeController) {
    val lines = lines.renameUpdatedQuests()
    val presets = mutableListOf<Array<Any?>>()
    val orders = mutableListOf<Array<Any?>>()
    val visibilities = mutableListOf<Array<Any?>>()
    // set of lines to avoid duplicates that might arise when user has quests of old and new name in the backup
    val presetsSet = hashSetOf<String>()
    val ordersSet = hashSetOf<String>()
    val visibilitiesSet = hashSetOf<String>()
    var currentThing = BACKUP_PRESETS
    val profileIdMap = mutableMapOf(0L to 0L) // "default" is not in the presets section
    val qsRegex = "(\\d+)_qs_".toRegex()
    for (line in lines) { // go through list of presets
        val split = line.split(",")
        if (split.size < 2) break // happens if we come to the next category
        val id = split[0].toLong()
        profileIdMap[id] = id
    }

    if (!replaceExistingPresets) {
        // map profile ids to ids greater than existing maximum
        val max = db.query(EditTypePresetsTable.NAME) { it.getLong(EditTypePresetsTable.Columns.EDIT_TYPE_PRESET_ID) }.maxOrNull() ?: 0L
        val keys = profileIdMap.keys.toList()
        keys.forEachIndexed { i, id ->
            profileIdMap[id] = max + i + 1L
        }
        // consider that profile 0 has no name, as it's the "default"
        presets.add(arrayOf(profileIdMap[0L]!!, "Default"))
    }

    val questSettingsLines = mutableListOf<String>()
    for (line in lines) {
        if (line.isEmpty()) continue // happens if a section is completely empty
        if (line == BACKUP_PRESETS_ORDERS || line == BACKUP_PRESETS_VISIBILITIES) {
            currentThing = line
            continue
        }
        if (line == BACKUP_PRESETS_QUEST_SETTINGS) {
            try {
                // get remaining lines (they must be written if BACKUP_PRESETS_QUEST_SETTINGS is written)
                val l = lines.subList(lines.indexOf(line) + 1, lines.size)
                // replace per-preset quest settings preset ids
                val adjustedLines = l.map { it.replace(qsRegex) { result ->
                    if (result.groupValues.size > 1)
                        "${result.groupValues[1].toLongOrNull()?.let { profileIdMap[it] }}_qs_"
                    else throw (IllegalStateException())
                } }
                questSettingsLines.addAll(adjustedLines)
            } catch (_: Exception){
                // do nothing if lines are broken somehow
            }
            break
        }
        val split = line.split(",")
        if (split.size < 2) break
        val id = profileIdMap[split[0].toLong()]!!
        when (currentThing) {
            BACKUP_PRESETS -> if (presetsSet.add(line)) presets.add(arrayOf(id, split[1]))
            BACKUP_PRESETS_ORDERS -> if (ordersSet.add(line)) orders.add(arrayOf(id, split[1], split[2]))
            BACKUP_PRESETS_VISIBILITIES -> if (visibilitiesSet.add(line)) visibilities.add(arrayOf(id, split[1], split[2].toLong()))
        }
    }

    db.transaction {
        if (replaceExistingPresets) {
            // delete existing data in all tables
            db.delete(EditTypePresetsTable.NAME)
            db.delete(QuestTypeOrderTable.NAME)
            db.delete(VisibleEditTypeTable.NAME)
        }
        db.insertMany(EditTypePresetsTable.NAME,
            arrayOf(EditTypePresetsTable.Columns.EDIT_TYPE_PRESET_ID, EditTypePresetsTable.Columns.EDIT_TYPE_PRESET_NAME),
            presets
        )
        db.insertMany(QuestTypeOrderTable.NAME,
            arrayOf(QuestTypeOrderTable.Columns.EDIT_TYPE_PRESET_ID,
                QuestTypeOrderTable.Columns.BEFORE,
                QuestTypeOrderTable.Columns.AFTER),
            orders
        )
        db.insertMany(VisibleEditTypeTable.NAME,
            arrayOf(VisibleEditTypeTable.Columns.EDIT_TYPE_PRESET_ID,
                VisibleEditTypeTable.Columns.EDIT_TYPE,
                VisibleEditTypeTable.Columns.VISIBILITY),
            visibilities
        )
    }

    // database stuff successful, update preferences
    if (replaceExistingPresets) {
        val prefs = StreetCompleteApplication.preferences
        prefs.edit {
            // remove all per-preset quest settings for proper replace
            prefs.all.keys.filter { qsRegex.containsMatchIn(it) }.forEach { remove(it) }
            // set selected preset to default, because previously selected may not exist any more
            putLong(Preferences.SELECTED_EDIT_TYPE_PRESET, 0)
        }
    }
    readToSettings(questSettingsLines)

    visibleEditTypeController.setVisibilities(emptyMap()) // reload stuff
}

private fun importSettings(uri: Uri, activity: Activity, osmoseDao: OsmoseDao, externalSourceQuestController: ExternalSourceQuestController): Boolean {
    val lines = activity.contentResolver?.openInputStream(uri)?.use { it.reader().readLines().renameUpdatedQuests() } ?: return false
    val r = readToSettings(lines)
    osmoseDao.reloadIgnoredItems()
    externalSourceQuestController.invalidate()
    return r
}
