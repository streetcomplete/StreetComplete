package de.westnordost.streetcomplete.screens.settings

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.Cleaner
import de.westnordost.streetcomplete.data.ConflictAlgorithm
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestsHiddenTable
import de.westnordost.streetcomplete.data.osmnotes.notequests.NoteQuestsHiddenTable
import de.westnordost.streetcomplete.data.othersource.OtherSourceQuestTables
import de.westnordost.streetcomplete.data.visiblequests.QuestPresetsTable
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderTable
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeController
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeTable
import de.westnordost.streetcomplete.quests.external.FILENAME_EXTERNAL
import de.westnordost.streetcomplete.quests.tree.FILENAME_TREES
import de.westnordost.streetcomplete.screens.HasTitle
import de.westnordost.streetcomplete.util.ktx.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.koin.android.ext.android.inject
import java.io.File
import java.io.IOException
import kotlin.system.exitProcess

class DataManagementSettingsFragment :
    PreferenceFragmentCompat(),
    HasTitle,
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val prefs: SharedPreferences by inject()
    private val db: Database by inject()
    private val visibleQuestTypeController: VisibleQuestTypeController by inject()
    private val cleaner: Cleaner by inject()

    override val title: String get() = getString(R.string.pref_screen_data_management)

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        PreferenceManager.setDefaultValues(requireContext(), R.xml.preferences_ee_data_management, false)
        addPreferencesFromResource(R.xml.preferences_ee_data_management)

        findPreference<Preference>("export")?.setOnPreferenceClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.pref_export)
                .setNegativeButton(R.string.import_export_settings) { _,_ -> export("settings") }
                .setNeutralButton(R.string.import_export_hidden_quests) { _,_ -> export("hidden_quests") }
                .setPositiveButton(R.string.import_export_presets)  { _,_ -> export("presets") }
                .show()

            true
        }

        findPreference<Preference>("import")?.setOnPreferenceClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.pref_import)
                .setMessage(R.string.import_warning)
                .setNegativeButton(R.string.import_export_settings) { _,_ -> import("settings") }
                .setNeutralButton(R.string.import_export_hidden_quests) { _,_ -> import("hidden_quests") }
                .setPositiveButton(R.string.import_export_presets)  { _,_ -> import("presets") }
                .show()

            true
        }

        findPreference<Preference>("trees")?.setOnPreferenceClickListener {
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle(R.string.pref_trees_title)
                .setMessage(R.string.tree_external_import_export_message)
                .setNeutralButton(android.R.string.cancel, null)
                .setNegativeButton(R.string.tree_external_import) { _,_ -> import("trees") }
                .setPositiveButton(R.string.tree_external_export)  { _,_ -> export("trees") }
                .show()

            val treesFile = File(context?.getExternalFilesDir(null), FILENAME_TREES)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = treesFile.exists()

            true
        }

        findPreference<Preference>("external")?.setOnPreferenceClickListener {
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle(R.string.pref_external_title)
                .setMessage(R.string.tree_external_import_export_message)
                .setNeutralButton(android.R.string.cancel, null)
                .setNegativeButton(R.string.tree_external_import) { _,_ -> import("external") }
                .setPositiveButton(R.string.tree_external_export)  { _,_ -> export("external") }
                .show()

            val treesFile = File(context?.getExternalFilesDir(null), FILENAME_EXTERNAL)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = treesFile.exists()

            true
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            Prefs.DATA_RETAIN_TIME -> { lifecycleScope.launch(Dispatchers.IO) { cleaner.clean() } }
            Prefs.PREFER_EXTERNAL_SD -> { moveMapTilesToCurrentLocation() }
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
            REQUEST_CODE_SETTINGS_EXPORT -> {
                val f = File(context?.applicationInfo?.dataDir + File.separator + "shared_prefs" + File.separator + context?.applicationInfo?.packageName + "_preferences.xml")
                if (!f.exists()) {
                    context?.toast(R.string.export_error_no_settings_file, Toast.LENGTH_LONG)
                    return
                }
                activity?.contentResolver?.openOutputStream(uri)?.use { os ->
                    val lines = f.readLines().filterNot { // ignore login data and sprite sheets
                        it.contains("TangramPinsSpriteSheet") || it.contains("TangramIconsSpriteSheet") || it.contains("oauth.") || it.contains("osm.")
                    }
                    os.bufferedWriter().use { it.write(lines.joinToString("\n")) }
                }
                // there is some SharedPreferencesBackupHelper, but can't access this without some app backup thing apparently
            }
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
                    val hiddenOtherSourceQuests = db.query(OtherSourceQuestTables.NAME_HIDDEN) { c ->
                        c.getString(OtherSourceQuestTables.Columns.SOURCE) + "," +
                            c.getString(OtherSourceQuestTables.Columns.ID) + "," +
                            c.getLong(OtherSourceQuestTables.Columns.TIMESTAMP)
                    }

                    os.bufferedWriter().use {
                        it.write(version.toString())
                        it.write("\n$BACKUP_HIDDEN_OSM_QUESTS\n")
                        it.write(hiddenOsmQuests.joinToString("\n"))
                        it.write("\n$BACKUP_HIDDEN_NOTES\n")
                        it.write(hiddenNotes.joinToString("\n"))
                        it.write("\n$BACKUP_HIDDEN_OTHER_QUESTS\n")
                        it.write(hiddenOtherSourceQuests.joinToString("\n"))
                    }
                }
            }
            REQUEST_CODE_PRESETS_EXPORT -> {
                activity?.contentResolver?.openOutputStream(uri)?.use { os ->
                    val version = db.rawQuery("PRAGMA user_version;") { c -> c.getLong("user_version") }.single()
                    if (version > LAST_KNOWN_DB_VERSION)
                        context?.toast(getString(R.string.export_warning_db_version), Toast.LENGTH_LONG)

                    val presets = db.query(QuestPresetsTable.NAME) { c ->
                        c.getLong(QuestPresetsTable.Columns.QUEST_PRESET_ID).toString() + "," +
                            c.getString(QuestPresetsTable.Columns.QUEST_PRESET_NAME)
                    }
                    val orders = db.query(QuestTypeOrderTable.NAME) { c->
                        c.getLong(QuestTypeOrderTable.Columns.QUEST_PRESET_ID).toString() + "," +
                            c.getString(QuestTypeOrderTable.Columns.BEFORE) + "," +
                            c.getString(QuestTypeOrderTable.Columns.AFTER)
                    }
                    val visibilities = db.query(VisibleQuestTypeTable.NAME) { c ->
                        c.getLong(VisibleQuestTypeTable.Columns.QUEST_PRESET_ID).toString() + "," +
                            c.getString(VisibleQuestTypeTable.Columns.QUEST_TYPE) + "," +
                            c.getLong(VisibleQuestTypeTable.Columns.VISIBILITY).toString()
                    }

                    os.bufferedWriter().use {
                        it.write(version.toString())
                        it.write("\n$BACKUP_PRESETS\n")
                        it.write(presets.joinToString("\n"))
                        it.write("\n$BACKUP_PRESETS_ORDERS\n")
                        it.write(orders.joinToString("\n"))
                        it.write("\n$BACKUP_PRESETS_VISIBILITIES\n")
                        it.write(visibilities.joinToString("\n"))
                    }
                }
            }
            REQUEST_CODE_SETTINGS_IMPORT -> {
                val f = File(context?.applicationInfo?.dataDir + File.separator + "shared_prefs" + File.separator + context?.applicationInfo?.packageName + "_preferences.xml")
                val t = activity?.contentResolver?.openInputStream(uri)?.use { it.reader().readLines() }
                if (t == null || t.firstOrNull()?.startsWith("<?xml version") != true) {
                    context?.toast(getString(R.string.import_error), Toast.LENGTH_LONG)
                    return
                }
                f.writeText(t.filterNot { it.contains("TangramPinsSpriteSheet") || it.contains("TangramIconsSpriteSheet") }.joinToString("\n"))
                // need to immediately restart the app to avoid current settings writing to new file
                restartApp()
            }
            REQUEST_CODE_HIDDEN_IMPORT -> {
                // do not delete existing hidden quests; this can be done manually anyway
                val lines = importLinesAndCheck(uri, BACKUP_HIDDEN_OSM_QUESTS)

                val quests = mutableListOf<Array<Any?>>()
                val notes = mutableListOf<Array<Any?>>()
                val otherSourceQuests = mutableListOf<Array<Any?>>()
                var currentThing = BACKUP_HIDDEN_OSM_QUESTS
                for (line in lines) {

                    if (line.isEmpty()) continue // happens if a section is completely empty
                    if (line == BACKUP_HIDDEN_NOTES || line == BACKUP_HIDDEN_OTHER_QUESTS) {
                        currentThing = line
                        continue
                    }
                    val split = line.split(",")
                    if (split.size < 2) break
                    when (currentThing) {
                        BACKUP_HIDDEN_OSM_QUESTS -> quests.add(arrayOf(split[0].toLong(), split[1], split[2], split[3].toLong()))
                        BACKUP_HIDDEN_NOTES -> notes.add(arrayOf(split[0].toLong(), split[1].toLong()))
                        BACKUP_HIDDEN_OTHER_QUESTS -> otherSourceQuests.add(arrayOf(split[0], split[1], split[2].toLong()))
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
                db.insertMany(OtherSourceQuestTables.NAME_HIDDEN,
                    arrayOf(OtherSourceQuestTables.Columns.SOURCE,
                        OtherSourceQuestTables.Columns.ID,
                        OtherSourceQuestTables.Columns.TIMESTAMP),
                    otherSourceQuests,
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
                    context?.toast(getString(R.string.import_error), Toast.LENGTH_LONG)
                    return
                }
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.pref_import)
                    .setMessage(R.string.import_presets_message)
                    .setPositiveButton(R.string.import_presets_replace) { _, _ -> importPresets(lines, true) }
                    .setNeutralButton(R.string.import_presets_add) { _, _ -> importPresets(lines, false) }
                    .show()
            }
            REQUEST_CODE_EXTERNAL_IMPORT -> { readFromUriToExternalFile(uri, FILENAME_EXTERNAL) }
            REQUEST_CODE_EXTERNAL_EXPORT -> { writeFromExternalFileToUri(FILENAME_EXTERNAL, uri) }
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
            "trees" -> REQUEST_CODE_TREES_IMPORT
            "external" -> REQUEST_CODE_EXTERNAL_IMPORT
            else -> throw(IllegalArgumentException())
        }
        startActivityForResult(intent, requestCode)
    }

    private fun export(name: String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            val fileName = if (name in arrayOf("trees", "external")) "$name.csv" else "$name.txt"
            putExtra(Intent.EXTRA_TITLE, fileName)
            type = "application/text"
        }
        val requestCode = when (name) {
            "settings" -> REQUEST_CODE_SETTINGS_EXPORT
            "hidden_quests" ->  REQUEST_CODE_HIDDEN_EXPORT
            "presets" ->  REQUEST_CODE_PRESETS_EXPORT
            "trees" -> REQUEST_CODE_TREES_EXPORT
            "external" -> REQUEST_CODE_EXTERNAL_EXPORT
            else -> throw(IllegalArgumentException())
        }
        startActivityForResult(intent, requestCode)
    }

    /** @returns the lines after [checkLine], which is expected to be the second line */
    private fun importLinesAndCheck(uri: Uri, checkLine: String): List<String> =
        activity?.contentResolver?.openInputStream(uri)?.use { it.bufferedReader().use { input ->
            val fileVersion = input.readLine().toLongOrNull()
            if (fileVersion == null || input.readLine() != checkLine) {
                context?.toast(getString(R.string.import_error), Toast.LENGTH_LONG)
                return emptyList()
            }
            val dbVersion = db.rawQuery("PRAGMA user_version;") { c -> c.getLong("user_version") }.single()
            if (fileVersion != dbVersion && (fileVersion > LAST_KNOWN_DB_VERSION || dbVersion > LAST_KNOWN_DB_VERSION)) {
                context?.toast(getString(R.string.import_error_db_version), Toast.LENGTH_LONG)
                return emptyList()
            }
            input.readLines()
        } } ?: emptyList()

    private fun importPresets(lines: List<String>, replaceExistingPresets: Boolean) {
        val presets = mutableListOf<Array<Any?>>()
        val orders = mutableListOf<Array<Any?>>()
        val visibilities = mutableListOf<Array<Any?>>()
        var currentThing = BACKUP_PRESETS
        val profileIdMap = mutableMapOf(0L to 0L) // "default" is not in the presets section
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

        for (line in lines) {
            if (line.isEmpty()) continue // happens if a section is completely empty
            if (line == BACKUP_PRESETS_ORDERS || line == BACKUP_PRESETS_VISIBILITIES) {
                currentThing = line
                continue
            }
            val split = line.split(",")
            if (split.size < 2) break
            val id = profileIdMap[split[0].toLong()]!!
            when (currentThing) {
                BACKUP_PRESETS -> presets.add(arrayOf(id, split[1]))
                BACKUP_PRESETS_ORDERS -> orders.add(arrayOf(id, split[1], split[2]))
                BACKUP_PRESETS_VISIBILITIES -> visibilities.add(arrayOf(id, split[1], split[2].toLong()))
            }
        }

        if (replaceExistingPresets) {
            // delete existing data in both tables
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

        if (replaceExistingPresets) // set selected preset to default, because previously selected may not exist any more
            prefs.edit().putLong(Prefs.SELECTED_QUESTS_PRESET, 0).apply()
        visibleQuestTypeController.onQuestTypeVisibilitiesChanged()
    }

    private fun readFromUriToExternalFile(uri: Uri, filename: String) =
        activity?.contentResolver?.openInputStream(uri)?.use { it.bufferedReader().use { reader ->
            File(context?.getExternalFilesDir(null), filename).writeText(reader.readText())
        } }

    private fun writeFromExternalFileToUri(filename: String, uri: Uri) =
        activity?.contentResolver?.openOutputStream(uri)?.use { it.bufferedWriter().use { writer ->
            writer.write(File(context?.getExternalFilesDir(null), filename).readText())
        } }

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

private const val REQUEST_CODE_SETTINGS_EXPORT = 532527
private const val REQUEST_CODE_HIDDEN_EXPORT = 532528
private const val REQUEST_CODE_PRESETS_EXPORT = 532529
private const val REQUEST_CODE_SETTINGS_IMPORT = 67367487
private const val REQUEST_CODE_HIDDEN_IMPORT = 67367488
private const val REQUEST_CODE_PRESETS_IMPORT = 67367489
private const val REQUEST_CODE_TREES_IMPORT = 5331
private const val REQUEST_CODE_TREES_EXPORT = 5332
private const val REQUEST_CODE_EXTERNAL_IMPORT = 5333
private const val REQUEST_CODE_EXTERNAL_EXPORT = 5334

private const val LAST_KNOWN_DB_VERSION = 6L // TODO: adjust this every time the version changes and handle changes!

private const val BACKUP_HIDDEN_OSM_QUESTS = "quests"
private const val BACKUP_HIDDEN_NOTES = "notes"
private const val BACKUP_HIDDEN_OTHER_QUESTS = "other_source_quests"
private const val BACKUP_PRESETS = "presets"
private const val BACKUP_PRESETS_ORDERS = "orders"
private const val BACKUP_PRESETS_VISIBILITIES = "visibilities"
