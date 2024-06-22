package de.westnordost.streetcomplete.screens.settings

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.StreetCompleteApplication
import de.westnordost.streetcomplete.data.Cleaner
import de.westnordost.streetcomplete.data.ConflictAlgorithm
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuestController
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestsHiddenTable
import de.westnordost.streetcomplete.data.osmnotes.notequests.NoteQuestsHiddenTable
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuestTables
import de.westnordost.streetcomplete.data.urlconfig.UrlConfigController
import de.westnordost.streetcomplete.data.visiblequests.QuestPreset
import de.westnordost.streetcomplete.data.visiblequests.QuestPresetsController
import de.westnordost.streetcomplete.data.visiblequests.QuestPresetsTable
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderTable
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeController
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeTable
import de.westnordost.streetcomplete.overlays.custom.getCustomOverlayIndices
import de.westnordost.streetcomplete.overlays.custom.getIndexedCustomOverlayPref
import de.westnordost.streetcomplete.quests.custom.FILENAME_CUSTOM_QUEST
import de.westnordost.streetcomplete.quests.osmose.OsmoseDao
import de.westnordost.streetcomplete.quests.tree.FILENAME_TREES
import de.westnordost.streetcomplete.screens.HasTitle
import de.westnordost.streetcomplete.util.TempLogger
import de.westnordost.streetcomplete.util.dialogs.setViewWithDefaultPadding
import de.westnordost.streetcomplete.util.getFakeCustomOverlays
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.util.logs.DatabaseLogger
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject
import java.io.BufferedWriter
import java.io.File
import java.io.IOException
import kotlin.system.exitProcess

class DataManagementSettingsFragment :
    PreferenceFragmentCompat(),
    HasTitle,
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val prefs = StreetCompleteApplication.preferences
    private val scPrefs: ObservableSettings by inject()
    private val db: Database by inject()
    private val visibleQuestTypeController: VisibleQuestTypeController by inject()
    private val cleaner: Cleaner by inject()
    private val urlConfigController: UrlConfigController by inject()
    private val questPresetsController: QuestPresetsController by inject()
    private val databaseLogger: DatabaseLogger by inject()
    private val osmoseDao: OsmoseDao by inject()
    private val externalSourceQuestController: ExternalSourceQuestController by inject()

    override val title: String get() = getString(R.string.pref_screen_data_management)

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        PreferenceManager.setDefaultValues(requireContext(), R.xml.preferences_ee_data_management, false)
        addPreferencesFromResource(R.xml.preferences_ee_data_management)

        if (!BuildConfig.DEBUG && !prefs.getBoolean(Prefs.TEMP_LOGGER, false))
            findPreference<Preference>("temp_logger")?.isVisible = false

        fun importExport(import: Boolean) {
            val lists = listOf(
                resources.getString(R.string.import_export_hidden_quests) to "hidden_quests",
                resources.getString(R.string.import_export_presets) to "presets",
                resources.getString(R.string.import_export_custom_overlays) to "overlays",
                resources.getString(R.string.import_export_settings) to "settings",
            ).unzip()
            AlertDialog.Builder(requireContext())
                .setTitle(if (import) R.string.pref_import else R.string.pref_export)
                .setItems(lists.first.toTypedArray()) { _, i -> if (import) import(lists.second[i]) else export(lists.second[i]) }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        findPreference<Preference>("export")?.setOnPreferenceClickListener {
            importExport(false)
            true
        }

        findPreference<Preference>("import")?.setOnPreferenceClickListener {
            importExport(true)
            true
        }

        findPreference<Preference>("trees")?.setOnPreferenceClickListener {
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle(R.string.pref_trees_title)
                .setMessage(R.string.tree_custom_quest_import_export_message)
                .setNeutralButton(android.R.string.cancel, null)
                .setNegativeButton(R.string.tree_custom_quest_import) { _,_ -> import("trees") }
                .setPositiveButton(R.string.tree_custom_quest_export)  { _,_ -> export("trees") }
                .show()

            val treesFile = File(context?.getExternalFilesDir(null), FILENAME_TREES)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = treesFile.exists()

            true
        }

        findPreference<Preference>("custom_quest")?.setOnPreferenceClickListener {
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle(R.string.pref_custom_title)
                .setMessage(R.string.tree_custom_quest_import_export_message)
                .setNeutralButton(android.R.string.cancel, null)
                .setNegativeButton(R.string.tree_custom_quest_import) { _,_ -> import("custom_quest") }
                .setPositiveButton(R.string.tree_custom_quest_export)  { _,_ -> export("custom_quest") }
                .show()

            val treesFile = File(context?.getExternalFilesDir(null), FILENAME_CUSTOM_QUEST)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = treesFile.exists()

            true
        }

        findPreference<Preference>("raster_tile_url")?.setOnPreferenceClickListener {
            var d: AlertDialog? = null
            val currentUrl = prefs.getString(Prefs.RASTER_TILE_URL, "https://server.arcgisonline.com/arcgis/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}")!!
            val urlText = EditText(requireContext()).apply {
                setText(currentUrl)
                doAfterTextChanged {
                    val t = it.toString()
                    d?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = t.contains("{x}") && t.contains("{y}") && t.contains("{z}")
                }
            }
            val hideLabelsSwitch = SwitchCompat(requireContext()).apply {
                setText(R.string.pref_tile_source_hide_labels)
                isChecked = prefs.getBoolean(Prefs.NO_SATELLITE_LABEL, false)
            }
            val layout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                addView(TextView(requireContext()).apply { setText(R.string.pref_tile_source_message) })
                addView(urlText)
                addView(hideLabelsSwitch)
            }
            d = AlertDialog.Builder(requireContext())
                .setTitle(R.string.pref_tile_source_title)
                .setViewWithDefaultPadding(layout)
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton(R.string.action_reset) { _, _ ->
                    prefs.edit {
                        remove(Prefs.RASTER_TILE_URL)
                        remove(Prefs.NO_SATELLITE_LABEL)
                    }
                }
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    prefs.edit {
                        putString(Prefs.RASTER_TILE_URL, urlText.text.toString())
                        putBoolean(Prefs.NO_SATELLITE_LABEL, hideLabelsSwitch.isChecked)
                    }
                    activity?.let { ActivityCompat.recreate(it) } // need to reload scene
                }
                .create()
            d.show()
            true
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        when (key) {
            Prefs.DATA_RETAIN_TIME -> { lifecycleScope.launch(Dispatchers.IO) { cleaner.cleanOld() } }
            Prefs.PREFER_EXTERNAL_SD -> { moveMapTilesToCurrentLocation() }
            Prefs.TEMP_LOGGER -> { if (prefs.getBoolean(Prefs.TEMP_LOGGER, false)) {
                Log.instances.removeAll { it is DatabaseLogger }
                Log.instances.add(TempLogger)
            } else {
                Log.instances.remove(TempLogger)
                Log.instances.add(databaseLogger)
            }}
        }
    }

    override fun onResume() {
        super.onResume()
        prefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        prefs.unregisterOnSharedPreferenceChangeListener(this)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK || data == null)
            return
        val uri = data.data ?: return
        when (requestCode) {
            REQUEST_CODE_SETTINGS_EXPORT -> exportSettings(uri)
            REQUEST_CODE_HIDDEN_EXPORT -> {
                activity?.contentResolver?.openOutputStream(uri)?.use { os ->
                    val version = db.rawQuery("PRAGMA user_version;") { c -> c.getLong("user_version") }.single()
                    if (version > LAST_KNOWN_DB_VERSION)
                        context?.toast(getString(R.string.export_warning_db_version), Toast.LENGTH_LONG)

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
            REQUEST_CODE_PRESETS_EXPORT -> {
                val allPresets = mutableListOf<QuestPreset>()
                allPresets.add(QuestPreset(0, requireContext().getString(R.string.quest_presets_default_name)))
                allPresets.addAll(questPresetsController.getAll())
                val array = allPresets.map { it.name }.toTypedArray()
                val selectedPresets = mutableSetOf<Long>()
                val d = AlertDialog.Builder(requireContext())
                    .setTitle(R.string.import_export_presets_select)
                    .setMultiChoiceItems(array, null) { di, which, isChecked ->
                        if (isChecked) selectedPresets.add(allPresets[which].id)
                        else selectedPresets.remove(allPresets[which].id)
                        (di as AlertDialog).getButton(Dialog.BUTTON_POSITIVE)?.isEnabled = selectedPresets.isNotEmpty()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        exportPresets(selectedPresets, uri)
                    }
                    .show()
                d.getButton(Dialog.BUTTON_POSITIVE)?.isEnabled = false
            }
            REQUEST_CODE_OVERLAYS_EXPORT -> {
                val allOverlays = getFakeCustomOverlays(scPrefs, requireContext(), false)
                val array = allOverlays.map { it.changesetComment }.toTypedArray()
                val selectedOverlays = mutableSetOf<String>()
                val d = AlertDialog.Builder(requireContext())
                    .setTitle(R.string.import_export_custom_overlays_select)
                    .setMultiChoiceItems(array, null) { di, which, isChecked ->
                        if (isChecked) selectedOverlays.add(allOverlays[which].wikiLink!!)
                        else selectedOverlays.remove(allOverlays[which].wikiLink!!)
                        (di as AlertDialog).getButton(Dialog.BUTTON_POSITIVE)?.isEnabled = selectedOverlays.isNotEmpty()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        exportCustomOverlays(selectedOverlays, uri)
                    }
                    .show()
                d.getButton(Dialog.BUTTON_POSITIVE)?.isEnabled = false
            }
            REQUEST_CODE_SETTINGS_IMPORT -> if (!importSettings(uri)) context?.toast(getString(R.string.import_error), Toast.LENGTH_LONG)
            REQUEST_CODE_HIDDEN_IMPORT -> {
                // do not delete existing hidden quests; this can be done manually anyway
                val lines = importLinesAndCheck(uri, BACKUP_HIDDEN_OSM_QUESTS)

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
                visibleQuestTypeController.onQuestTypeVisibilitiesChanged()
                // imported hidden osmquests are applied, but don't show up in edit history
                // imported other quests are not even applied
            }
            REQUEST_CODE_PRESETS_IMPORT -> {
                val lines = importLinesAndCheck(uri, BACKUP_PRESETS)
                if (lines.isEmpty()) {
                    return
                }
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.pref_import)
                    .setMessage(R.string.import_presets_overlays_message)
                    .setPositiveButton(R.string.import_presets_overlays_replace) { _, _ -> importPresets(lines, true) }
                    .setNeutralButton(R.string.import_presets_overlays_add) { _, _ -> importPresets(lines, false) }
                    .show()
            }
            REQUEST_CODE_OVERLAYS_IMPORT -> {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.pref_import)
                    .setMessage(R.string.import_presets_overlays_message)
                    .setPositiveButton(R.string.import_presets_overlays_replace) { _, _ -> if (!importCustomOverlays(uri, true)) context?.toast(getString(R.string.import_error), Toast.LENGTH_LONG) }
                    .setNeutralButton(R.string.import_presets_overlays_add) { _, _ -> if (!importCustomOverlays(uri, false)) context?.toast(getString(R.string.import_error), Toast.LENGTH_LONG) }
                    .show()
            }
            REQUEST_CODE_CUSTOM_QUEST_IMPORT -> { readFromUriToExternalFile(uri, FILENAME_CUSTOM_QUEST) }
            REQUEST_CODE_CUSTOM_QUEST_EXPORT -> { writeFromExternalFileToUri(FILENAME_CUSTOM_QUEST, uri) }
            REQUEST_CODE_TREES_IMPORT -> { readFromUriToExternalFile(uri, FILENAME_TREES) }
            REQUEST_CODE_TREES_EXPORT -> { writeFromExternalFileToUri(FILENAME_TREES, uri) }
        }
    }

    private fun import(name: String) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*" // can't select text file if setting to application/text
        }
        val requestCode = when (name) {
            "settings" -> REQUEST_CODE_SETTINGS_IMPORT
            "hidden_quests" ->  REQUEST_CODE_HIDDEN_IMPORT
            "presets" ->  REQUEST_CODE_PRESETS_IMPORT
            "overlays" ->  REQUEST_CODE_OVERLAYS_IMPORT
            "trees" -> REQUEST_CODE_TREES_IMPORT
            "custom_quest" -> REQUEST_CODE_CUSTOM_QUEST_IMPORT
            else -> throw(IllegalArgumentException())
        }
        startActivityForResult(intent, requestCode)
    }

    private fun export(name: String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            val fileName = if (name in arrayOf("trees", "custom_quest")) "$name.csv" else "$name.txt"
            putExtra(Intent.EXTRA_TITLE, fileName)
            type = "application/text"
        }
        val requestCode = when (name) {
            "settings" -> REQUEST_CODE_SETTINGS_EXPORT
            "hidden_quests" ->  REQUEST_CODE_HIDDEN_EXPORT
            "presets" ->  REQUEST_CODE_PRESETS_EXPORT
            "overlays" ->  REQUEST_CODE_OVERLAYS_EXPORT
            "trees" -> REQUEST_CODE_TREES_EXPORT
            "custom_quest" -> REQUEST_CODE_CUSTOM_QUEST_EXPORT
            else -> throw(IllegalArgumentException())
        }
        startActivityForResult(intent, requestCode)
    }

    /** @returns the lines after [checkLine], which is expected to be the second or third line */
    private fun importLinesAndCheck(uri: Uri, checkLine: String): List<String> =
        activity?.contentResolver?.openInputStream(uri)?.use { it.bufferedReader().use { input ->
            val fileVersion = input.readLine().toLongOrNull()
            if (fileVersion == null || (input.readLine() != checkLine && input.readLine() != checkLine)) {
                Log.w(TAG, "import error, file version $fileVersion, checkLine $checkLine")
                context?.toast(getString(R.string.import_error), Toast.LENGTH_LONG)
                return emptyList()
            }
            val dbVersion = db.rawQuery("PRAGMA user_version;") { c -> c.getLong("user_version") }.single()
            if (fileVersion != dbVersion && (fileVersion > LAST_KNOWN_DB_VERSION || dbVersion > LAST_KNOWN_DB_VERSION)) {
                Log.w(TAG, "import error, file version $fileVersion, dbVersion $dbVersion, last known db version $LAST_KNOWN_DB_VERSION")
                context?.toast(getString(R.string.import_error_db_version), Toast.LENGTH_LONG)
                return emptyList()
            }
            input.readLines().renameUpdatedQuests()
        } } ?: emptyList()

    private fun importPresets(lines: List<String>, replaceExistingPresets: Boolean) {
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
            val max = db.query(QuestPresetsTable.NAME) { it.getLong(QuestPresetsTable.Columns.QUEST_PRESET_ID) }.maxOrNull() ?: 0L
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
                db.delete(QuestPresetsTable.NAME)
                db.delete(QuestTypeOrderTable.NAME)
                db.delete(VisibleQuestTypeTable.NAME)
            }
            db.insertMany(QuestPresetsTable.NAME,
                arrayOf(QuestPresetsTable.Columns.QUEST_PRESET_ID, QuestPresetsTable.Columns.QUEST_PRESET_NAME),
                presets
            )
            db.insertMany(QuestTypeOrderTable.NAME,
                arrayOf(QuestTypeOrderTable.Columns.QUEST_PRESET_ID,
                    QuestTypeOrderTable.Columns.BEFORE,
                    QuestTypeOrderTable.Columns.AFTER),
                orders
            )
            db.insertMany(VisibleQuestTypeTable.NAME,
                arrayOf(VisibleQuestTypeTable.Columns.QUEST_PRESET_ID,
                    VisibleQuestTypeTable.Columns.QUEST_TYPE,
                    VisibleQuestTypeTable.Columns.VISIBILITY),
                visibilities
            )
        }

        // database stuff successful, update preferences
        if (replaceExistingPresets) {
            prefs.edit {
                // remove all per-preset quest settings for proper replace
                prefs.all.keys.filter { qsRegex.containsMatchIn(it) }.forEach { remove(it) }
                // set selected preset to default, because previously selected may not exist any more
                putLong(Prefs.SELECTED_QUESTS_PRESET, 0)
            }
        }
        readToSettings(questSettingsLines)

        visibleQuestTypeController.setVisibilities(emptyMap()) // reload stuff
    }

    private fun exportPresets(ids: Collection<Long>, uri: Uri) {
        activity?.contentResolver?.openOutputStream(uri)?.use { os ->
            val version = db.rawQuery("PRAGMA user_version;") { c -> c.getLong("user_version") }.single()
            if (version > LAST_KNOWN_DB_VERSION)
                context?.toast(getString(R.string.export_warning_db_version), Toast.LENGTH_LONG)

            val presetString = ids.joinToString(",")
            val presets = db.query(QuestPresetsTable.NAME, where = "${QuestPresetsTable.Columns.QUEST_PRESET_ID} IN ($presetString)") { c ->
                c.getLong(QuestPresetsTable.Columns.QUEST_PRESET_ID).toString() + "," +
                    c.getString(QuestPresetsTable.Columns.QUEST_PRESET_NAME)
            }.map { "$it,${urlConfigController.create(it.substringBefore(',').toLong())}" }
            val orders = db.query(QuestTypeOrderTable.NAME, where = "${QuestTypeOrderTable.Columns.QUEST_PRESET_ID} IN ($presetString)") { c->
                c.getLong(QuestTypeOrderTable.Columns.QUEST_PRESET_ID).toString() + "," +
                    c.getString(QuestTypeOrderTable.Columns.BEFORE) + "," +
                    c.getString(QuestTypeOrderTable.Columns.AFTER)
            }
            val visibilities = db.query(VisibleQuestTypeTable.NAME, where = "${VisibleQuestTypeTable.Columns.QUEST_PRESET_ID} IN ($presetString)") { c ->
                c.getLong(VisibleQuestTypeTable.Columns.QUEST_PRESET_ID).toString() + "," +
                    c.getString(VisibleQuestTypeTable.Columns.QUEST_TYPE) + "," +
                    c.getLong(VisibleQuestTypeTable.Columns.VISIBILITY).toString()
            }
            val perPresetQuestSetting = "\\d+_qs_.+".toRegex()
            val questSettings = prefs.all.filterKeys { it.matches(perPresetQuestSetting) && it.substringBefore('_').toLongOrNull() in ids }

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

    private fun readFromUriToExternalFile(uri: Uri, filename: String) {
        try {
            activity?.contentResolver?.openInputStream(uri)?.use { it.bufferedReader().use { reader ->
                File(context?.getExternalFilesDir(null), filename).writeText(reader.readText())
            } }
        } catch (_: IOException) {
            AlertDialog.Builder(requireContext())
                .setMessage(R.string.pref_save_file_error)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
    }

    private fun writeFromExternalFileToUri(filename: String, uri: Uri) {
        try {
            activity?.contentResolver?.openOutputStream(uri)?.use { it.bufferedWriter().use { writer ->
                writer.write(File(context?.getExternalFilesDir(null), filename).readText())
            } }
        } catch (_: IOException) {
            AlertDialog.Builder(requireContext())
                .setMessage(R.string.pref_save_file_error)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
    }

    private fun exportSettings(uri: Uri) {
        val perPresetQuestSetting = "\\d+_qs_.+".toRegex()
        val settings = prefs.all.filterKeys {
            !it.contains("TangramPinsSpriteSheet") // this is huge and gets generated if missing anyway
                && !it.contains("TangramIconsSpriteSheet") // this is huge and gets generated if missing anyway
                && it != Prefs.OAUTH2_ACCESS_TOKEN // login
                && !it.contains("osm.") // login data
                && !it.matches(perPresetQuestSetting) // per-preset quest settings should be stored with presets, because preset id is never guaranteed to match
                && !it.startsWith("custom_overlay") // custom overlays are exported separately
        }
        activity?.contentResolver?.openOutputStream(uri)?.use { it.bufferedWriter().use { settingsToJsonStream(settings, it) } }
    }

    private fun exportCustomOverlays(indices: Collection<String>, uri: Uri) {
        val filterRegex = "custom_overlay_(?:${indices.joinToString("|")})_.*".toRegex()
        val settings = prefs.all.filterKeys { filterRegex.matches(it) }.toMutableMap()
        settings[Prefs.CUSTOM_OVERLAY_INDICES] = indices.joinToString(",")
        if (prefs.getInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, 0).toString() in indices)
            settings[Prefs.CUSTOM_OVERLAY_SELECTED_INDEX] = prefs.getInt(Prefs.CUSTOM_OVERLAY_SELECTED_INDEX, 0)
        activity?.contentResolver?.openOutputStream(uri)?.use { it.bufferedWriter().use {
            it.appendLine("overlays")
            settingsToJsonStream(settings, it)
        } }
    }

    private fun importCustomOverlays(uri: Uri, replaceExisting: Boolean): Boolean {
        val lines = activity?.contentResolver?.openInputStream(uri)?.use { it.reader().readLines() } ?: return false
        if (lines.first() != "overlays") return false
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

    private fun importSettings(uri: Uri): Boolean {
        val lines = activity?.contentResolver?.openInputStream(uri)?.use { it.reader().readLines().renameUpdatedQuests() } ?: return false
        val r = readToSettings(lines)
        osmoseDao.reloadIgnoredItems()
        externalSourceQuestController.invalidate()
        preferenceScreen.removeAll()
        onCreatePreferences(null, null)
        return r
    }

    private fun readToSettings(list: List<String>): Boolean {
        val i = list.iterator()
        val e = prefs.edit()
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

    private fun restartApp() {
        // exitProcess does actually restart with the activity below, which should always be MainActivity.
        // No idea how to come back to SettingsFragment automatically, or why it actually DOES
        //  return to SettingsFragment when calling this from onActivityResult (settings import)
        exitProcess(0)
    }

    private fun moveMapTilesToCurrentLocation() {
        val sd = requireContext().externalCacheDirs
            .firstOrNull { Environment.isExternalStorageRemovable(it) } ?: return
        val default = requireContext().externalCacheDir ?: return
        val sdCache = File(sd, "tile_cache")
        val defaultCache = File(default, "tile_cache")

        // move to preferred storage
        val sdPreferred = prefs.getBoolean(Prefs.PREFER_EXTERNAL_SD, false)
        var d: AlertDialog? = null
        val target = if (sdPreferred) sdCache else defaultCache
        val source = if (sdPreferred) defaultCache else sdCache
        if (!source.exists()) return
        target.mkdirs()
        val moveJob = lifecycleScope.launch(Dispatchers.IO) {
            // copyRecursively would be easier, but crashes with FileNotFoundException (even if I tell it to skip in that case, wtf?)
            val files = source.listFiles() ?: return@launch
            val size = files.size
            var i = 0
            for (f in files) {
                i++
                if (!f.exists() || f.isDirectory) continue
                val dstFile = File(target, f.toRelativeString(source))
                if (!coroutineContext.isActive) break
                if (i % 100 == 0)
                    activity?.runOnUiThread { d?.setMessage("$i / $size") }
                if (dstFile.exists()) continue
                try {
                    f.inputStream().use { input -> dstFile.outputStream().use { input.copyTo(it) } }
                    dstFile.setLastModified(f.lastModified())
                } catch (e: IOException) {
                    continue
                }
            }
            yield() // don't delete if moving was canceled
            kotlin.runCatching { source.deleteRecursively() }
            d?.dismiss()
            restartApp() // necessary for really changing cache directory
        }
        d = AlertDialog.Builder(requireContext())
            .setTitle(R.string.moving)
            .setMessage("0 / ?")
            .setNegativeButton(android.R.string.cancel) { _,_ -> moveJob.cancel() }
            .setCancelable(false)
            .show()
    }
}

// when importing, names should be updated!
private fun List<String>.renameUpdatedQuests() = map { it.renameUpdatedQuests() }

fun String.renameUpdatedQuests() = replace("ExternalQuest", "CustomQuest")
    .replace("AddPicnicTableCover", "AddAmenityCover")
val oldQuestNames = listOf("ExternalQuest", "AddPicnicTableCover")

private const val REQUEST_CODE_SETTINGS_EXPORT = 532527
private const val REQUEST_CODE_HIDDEN_EXPORT = 532528
private const val REQUEST_CODE_PRESETS_EXPORT = 532529
private const val REQUEST_CODE_OVERLAYS_EXPORT = 532530
private const val REQUEST_CODE_SETTINGS_IMPORT = 67367487
private const val REQUEST_CODE_HIDDEN_IMPORT = 67367488
private const val REQUEST_CODE_PRESETS_IMPORT = 67367489
private const val REQUEST_CODE_OVERLAYS_IMPORT = 67367490
private const val REQUEST_CODE_TREES_IMPORT = 5331
private const val REQUEST_CODE_TREES_EXPORT = 5332
private const val REQUEST_CODE_CUSTOM_QUEST_IMPORT = 5333
private const val REQUEST_CODE_CUSTOM_QUEST_EXPORT = 5334

const val LAST_KNOWN_DB_VERSION = 17L

private const val BACKUP_HIDDEN_OSM_QUESTS = "quests"
private const val BACKUP_HIDDEN_NOTES = "notes"
private const val BACKUP_HIDDEN_OTHER_QUESTS = "other_source_quests"
private const val BACKUP_PRESETS = "presets"
private const val BACKUP_PRESETS_ORDERS = "orders"
private const val BACKUP_PRESETS_VISIBILITIES = "visibilities"
private const val BACKUP_PRESETS_QUEST_SETTINGS = "quest_settings"

private const val TAG = "DataManagementSettingsFragment"
