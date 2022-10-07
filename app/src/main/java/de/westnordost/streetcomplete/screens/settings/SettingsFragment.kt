package de.westnordost.streetcomplete.screens.settings

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import de.westnordost.streetcomplete.ApplicationConstants.DELETE_OLD_DATA_AFTER_DAYS
import de.westnordost.streetcomplete.ApplicationConstants.REFRESH_DATA_AFTER
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.Cleaner
import de.westnordost.streetcomplete.data.ConflictAlgorithm
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesDao
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestsHiddenTable
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestController
import de.westnordost.streetcomplete.data.osmnotes.NoteController
import de.westnordost.streetcomplete.data.osmnotes.notequests.NoteQuestsHiddenTable
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestController
import de.westnordost.streetcomplete.data.othersource.OtherSourceQuestController
import de.westnordost.streetcomplete.data.othersource.OtherSourceQuestTables
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.visiblequests.DayNightQuestFilter
import de.westnordost.streetcomplete.data.visiblequests.QuestPresetsSource
import de.westnordost.streetcomplete.data.visiblequests.QuestPresetsTable
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderTable
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeController
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeSource
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeTable
import de.westnordost.streetcomplete.databinding.DialogDeleteCacheBinding
import de.westnordost.streetcomplete.quests.external.FILENAME_EXTERNAL
import de.westnordost.streetcomplete.quests.tree.FILENAME_TREES
import de.westnordost.streetcomplete.screens.HasTitle
import de.westnordost.streetcomplete.screens.measure.MeasureActivity
import de.westnordost.streetcomplete.screens.settings.debug.ShowLinksActivity
import de.westnordost.streetcomplete.screens.settings.debug.ShowQuestFormsActivity
import de.westnordost.streetcomplete.util.getSelectedLocales
import de.westnordost.streetcomplete.util.ktx.format
import de.westnordost.streetcomplete.util.ktx.getYamlObject
import de.westnordost.streetcomplete.util.ktx.purge
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.util.setDefaultLocales
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.koin.android.ext.android.inject
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.system.exitProcess

/** Shows the settings screen */
class SettingsFragment :
    PreferenceFragmentCompat(),
    HasTitle,
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val prefs: SharedPreferences by inject()
    private val downloadedTilesDao: DownloadedTilesDao by inject()
    private val noteController: NoteController by inject()
    private val mapDataController: MapDataController by inject()
    private val osmQuestController: OsmQuestController by inject()
    private val osmNoteQuestController: OsmNoteQuestController by inject()
    private val resurveyIntervalsUpdater: ResurveyIntervalsUpdater by inject()
    private val questTypeRegistry: QuestTypeRegistry by inject()
    private val visibleQuestTypeSource: VisibleQuestTypeSource by inject()
    private val questPresetsSource: QuestPresetsSource by inject()
    private val visibleQuestTypeController: VisibleQuestTypeController by inject()
    private val dayNightQuestFilter: DayNightQuestFilter by inject()
    private val db: Database by inject()
    private val cleaner: Cleaner by inject()
    private val otherSourceQuestController: OtherSourceQuestController by inject()

    interface Listener {
        fun onClickedQuestSelection()
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    override val title: String get() = getString(R.string.action_settings)

    @SuppressLint("ResourceType") // for nearby quests... though it could probably be done in a nicer way
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        PreferenceManager.setDefaultValues(requireContext(), R.xml.preferences, false)
        addPreferencesFromResource(R.xml.preferences)

        findPreference<Preference>("quests")?.setOnPreferenceClickListener {
            listener?.onClickedQuestSelection()
            true
        }

        findPreference<Preference>("delete_cache")?.setOnPreferenceClickListener {
            val dialogBinding = DialogDeleteCacheBinding.inflate(layoutInflater)
            dialogBinding.descriptionText.text = resources.getString(R.string.delete_cache_dialog_message2,
                (1.0 * REFRESH_DATA_AFTER / (24 * 60 * 60 * 1000)).format(Locale.getDefault(), 1),
                (1.0 * prefs.getInt(Prefs.DATA_RETAIN_TIME, DELETE_OLD_DATA_AFTER_DAYS)).format(Locale.getDefault(), 1)
            )
            AlertDialog.Builder(requireContext())
                .setView(dialogBinding.root)
                .setNeutralButton(R.string.delete_confirmation_both) { _, _ -> lifecycleScope.launch {
                    deleteTiles()
                    deleteCache()}
                }
                .setPositiveButton(R.string.delete_confirmation_tiles) { _, _ -> lifecycleScope.launch { deleteTiles() }}
                .setNegativeButton(R.string.delete_confirmation_data) { _, _ -> lifecycleScope.launch { deleteCache() }}
                .show()
            true
        }

        findPreference<Preference>("quests.restore.hidden")?.setOnPreferenceClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.restore_dialog_message)
                .setPositiveButton(R.string.restore_confirmation) { _, _ -> lifecycleScope.launch {
                    val hidden = unhideQuests()
                    context?.toast(getString(R.string.restore_hidden_success, hidden), Toast.LENGTH_LONG)
                } }
                .setNegativeButton(android.R.string.cancel, null)
                .show()

            true
        }

        findPreference<Preference>("debug")?.isVisible = BuildConfig.DEBUG

        findPreference<Preference>("debug.quests")?.setOnPreferenceClickListener {
            startActivity(Intent(context, ShowQuestFormsActivity::class.java))
            true
        }

        findPreference<Preference>("debug.links")?.setOnPreferenceClickListener {
            startActivity(Intent(context, ShowLinksActivity::class.java))
            true
        }

        findPreference<Preference>("debug.ar_measure_horizontal")?.setOnPreferenceClickListener {
            startActivity(MeasureActivity.createIntent(requireContext(), false))
            true
        }

        findPreference<Preference>("debug.ar_measure_vertical")?.setOnPreferenceClickListener {
            startActivity(MeasureActivity.createIntent(requireContext(), true))
            true
        }

        findPreference<Preference>("hide_notes_by")?.setOnPreferenceClickListener {
            val text = EditText(context)
            text.setText(prefs.getStringSet(Prefs.HIDE_NOTES_BY_USERS, emptySet())?.joinToString(","))
            text.setHint(R.string.pref_hide_notes_hint)
            text.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE

            val layout = LinearLayout(context)
            layout.setPadding(30,10,30,10)
            layout.addView(text)

            AlertDialog.Builder(requireContext())
                .setTitle(R.string.pref_hide_notes_message)
                .setView(layout)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val content = text.text.split(",").map { it.trim().lowercase() }.toSet()
                    prefs.edit().putStringSet(Prefs.HIDE_NOTES_BY_USERS, content).apply()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()

            true
        }

        findPreference<Preference>("advanced_resurvey")?.setOnPreferenceClickListener {
            val layout = LinearLayout(context)
            layout.setPadding(30,10,30,10)
            layout.orientation = LinearLayout.VERTICAL
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

            AlertDialog.Builder(requireContext())
                .setTitle(R.string.advanced_resurvey_title)
                .setView(layout)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    prefs.edit {
                        putString(Prefs.RESURVEY_DATE, dateEditText.text.toString())
                        putString(Prefs.RESURVEY_KEYS, keyEditText.text.toString())
                    }
                    resurveyIntervalsUpdater.update()
                }
                .show()
            true
        }

        findPreference<Preference>("get_gpx_notes")?.setOnPreferenceClickListener {
            if (File(requireContext().getExternalFilesDir(null), "notes.gpx").exists()) {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    putExtra(Intent.EXTRA_TITLE, "notes.zip")
                    type = "application/zip"
                }
                startActivityForResult(intent, GPX_REQUEST_CODE)
            } else {
                context?.toast(getString(R.string.pref_save_gpx_not_found), Toast.LENGTH_LONG)
            }
            true
        }

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

        findPreference<Preference>("show_nearby_quests")?.setOnPreferenceClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(R.string.pref_show_nearby_quests_title)
            val linearLayout = LinearLayout(context)
            linearLayout.orientation = LinearLayout.VERTICAL

            val buttons = RadioGroup(context)
            buttons.orientation = RadioGroup.VERTICAL
            buttons.addView(RadioButton(context).apply {
                setText(R.string.show_nearby_quests_disable)
                id = 0
            })
            buttons.addView(RadioButton(context).apply {
                setText(R.string.show_nearby_quests_visible)
                id = 1
            })
            buttons.addView(RadioButton(context).apply {
                setText(R.string.show_nearby_quests_all_types)
                id = 2
            })
            buttons.addView(RadioButton(context).apply {
                setText(R.string.show_nearby_quests_even_hidden)
                id = 3
            })
            buttons.check(prefs.getInt(Prefs.SHOW_NEARBY_QUESTS, 0))
            buttons.setOnCheckedChangeListener { _, _ ->
                if (buttons.checkedRadioButtonId in 0..3)
                    prefs.edit { putInt(Prefs.SHOW_NEARBY_QUESTS, buttons.checkedRadioButtonId) }
            }

            val distanceText = TextView(context).apply { setText(R.string.show_nearby_quests_distance) }

            val distance = EditText(context).apply {
                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                setText(prefs.getFloat(Prefs.SHOW_NEARBY_QUESTS_DISTANCE, 0.0f).toString())
            }
            linearLayout.addView(buttons)
            linearLayout.addView(distanceText)
            linearLayout.addView(distance)

            linearLayout.setPadding(30,10,30,10)
            builder.setView(linearLayout)
            builder.setPositiveButton(android.R.string.ok) { _, _ ->
                distance.text.toString().toFloatOrNull()?.let {
                    if (it in 0.0..10.0)
                        prefs.edit { putFloat(Prefs.SHOW_NEARBY_QUESTS_DISTANCE, it) }
                }
            }
            builder.show()
            true
        }

        buildLanguageSelector()
    }

    fun import(name: String) {
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

    fun export(name: String) {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK || data == null)
            return
        val uri = data.data ?: return
        when (requestCode) {
            GPX_REQUEST_CODE -> saveGpx(data)
            REQUEST_CODE_SETTINGS_EXPORT -> {
                val f = File(context?.applicationInfo?.dataDir + File.separator + "shared_prefs" + File.separator + context?.applicationInfo?.packageName + "_preferences.xml")
                if (!f.exists()) return
                activity?.contentResolver?.openOutputStream(uri)?.use { os ->
                    val lines = f.readLines().filterNot {
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
                // how to make sure shared prefs are re-read from the new file?
                // probably need to restart app on import
                val f = File(context?.applicationInfo?.dataDir + File.separator + "shared_prefs" + File.separator + context?.applicationInfo?.packageName + "_preferences.xml")
                val t = activity?.contentResolver?.openInputStream(uri)?.use { it.reader().readText() }
                if (t == null || !t.startsWith("<?xml version")) {
                    context?.toast(getString(R.string.import_error), Toast.LENGTH_LONG)
                    return
                }
                f.writeText(t)

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
            REQUEST_CODE_EXTERNAL_IMPORT -> {
                activity?.contentResolver?.openInputStream(uri)?.use { it.bufferedReader().use { reader ->
                    File(context?.getExternalFilesDir(null), FILENAME_EXTERNAL).writeText(reader.readText())
                } }
            }
            REQUEST_CODE_EXTERNAL_EXPORT -> {
                val text = File(context?.getExternalFilesDir(null), FILENAME_EXTERNAL).readText()
                activity?.contentResolver?.openOutputStream(uri)?.use { it.bufferedWriter().use { writer ->
                    writer.write(text)
                } }
            }
            REQUEST_CODE_TREES_IMPORT -> {
                activity?.contentResolver?.openInputStream(uri)?.use { it.bufferedReader().use { reader ->
                    File(context?.getExternalFilesDir(null), FILENAME_TREES).writeText(reader.readText())
                } }
            }
            REQUEST_CODE_TREES_EXPORT -> {
                val text = File(context?.getExternalFilesDir(null), FILENAME_TREES).readText()
                activity?.contentResolver?.openOutputStream(uri)?.use { it.bufferedWriter().use { writer ->
                    writer.write(text)
                } }
            }
        }
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

    private fun restartApp() {
        // exitProcess does actually restart with the activity below, which should always be MainActivity.
        // No idea how to come back to SettingsFragment automatically, or why it actually DOES
        //  return to SettingsFragment when calling this from onActivityResult (settings import)
        exitProcess(0)
    }

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

    private fun saveGpx(data: Intent) {
        val uri = data.data ?: return
        val output = activity?.contentResolver?.openOutputStream(uri) ?: return
        val os = output.buffered()
        try {
            // read gpx and extract images
            val filesDir = requireContext().getExternalFilesDir(null)
            val gpxFile = File(filesDir, "notes.gpx")
            val files = mutableListOf(gpxFile)
            val gpxText = gpxFile.readText(Charsets.UTF_8)
            val picturesDir = File(filesDir, "Pictures")
            // get all files in pictures dir and check whether they occur in gpxText
            if (picturesDir.isDirectory) {
                picturesDir.walk().forEach {
                    if (!it.isDirectory && gpxText.contains(it.name))
                        files.add(it)
                }
            }
            filesDir?.walk()?.forEach {
                if (it.name.startsWith("track_") && it.name.endsWith(".gpx") && gpxText.contains(it.name))
                    files.add(it)
            }

            // write to zip
            val zipStream = ZipOutputStream(os)
            files.forEach {
                val fileStream = FileInputStream(it).buffered()
                zipStream.putNextEntry(ZipEntry(it.name)) // is name the right thing?
                fileStream.copyTo(zipStream, 1024)
                fileStream.close()
                zipStream.closeEntry()
            }
            zipStream.close()
            files.forEach { it.delete() }
        } catch (e: IOException) {
            context?.toast(getString(R.string.pref_save_gpx_error), Toast.LENGTH_LONG)
        }
        os.close()
        output.close()
    }

    private fun buildLanguageSelector() {
        val entryValues = resources.getYamlObject<MutableList<String>>(R.raw.languages)
        val entries = entryValues.map {
            val locale = Locale.forLanguageTag(it)
            val name = locale.displayName
            val nativeName = locale.getDisplayName(locale)
            return@map nativeName + if (name != nativeName) " — $name" else ""
        }.toMutableList()

        // add default as first element
        entryValues.add(0, "")
        entries.add(0, getString(R.string.language_default))

        findPreference<ListPreference>("language.select")?.also {
            it.entries = entries.toTypedArray()
            it.entryValues = entryValues.toTypedArray()
        }
    }

    override fun onStart() {
        super.onStart()
        findPreference<Preference>("quests")?.summary = getQuestPreferenceSummary()
    }

    override fun onResume() {
        super.onResume()
        prefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onPause() {
        super.onPause()
        prefs.unregisterOnSharedPreferenceChangeListener(this)
    }

    @SuppressLint("InflateParams", "ApplySharedPref")
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            Prefs.AUTOSYNC -> {
                if (Prefs.Autosync.valueOf(prefs.getString(Prefs.AUTOSYNC, "ON")!!) != Prefs.Autosync.ON) {
                    AlertDialog.Builder(requireContext())
                        .setView(layoutInflater.inflate(R.layout.dialog_tutorial_upload, null))
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                }
            }
            Prefs.THEME_SELECT -> {
                val theme = Prefs.Theme.valueOf(prefs.getString(Prefs.THEME_SELECT, "AUTO")!!)
                AppCompatDelegate.setDefaultNightMode(theme.appCompatNightMode)
                activity?.let { ActivityCompat.recreate(it) }
            }
            Prefs.LANGUAGE_SELECT -> {
                setDefaultLocales(getSelectedLocales(requireContext()))
                activity?.let { ActivityCompat.recreate(it) }
            }
            Prefs.RESURVEY_INTERVALS -> {
                resurveyIntervalsUpdater.update()
            }
            Prefs.QUEST_GEOMETRIES -> {
                visibleQuestTypeController.onQuestTypeVisibilitiesChanged()
            }
            Prefs.DAY_NIGHT_BEHAVIOR -> {
                dayNightQuestFilter.reload()
                visibleQuestTypeController.onQuestTypeVisibilitiesChanged()
            }
            Prefs.QUEST_SETTINGS_PER_PRESET -> {
                // need to commit, because otherwise setting change is lost after the immediate restart
                prefs.edit().putBoolean(Prefs.QUEST_SETTINGS_PER_PRESET, prefs.getBoolean(Prefs.QUEST_SETTINGS_PER_PRESET, false)).commit()
                restartApp()
            }
            Prefs.DATA_RETAIN_TIME -> {
                lifecycleScope.launch(Dispatchers.IO) { cleaner.clean() }
            }
            Prefs.PREFER_EXTERNAL_SD -> {
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

    private suspend fun deleteCache() = withContext(Dispatchers.IO) {
        downloadedTilesDao.removeAll()
        mapDataController.clear()
        noteController.clear()
        questTypeRegistry.forEach { it.deleteMetadataOlderThan(System.currentTimeMillis()) }
    }

    private suspend fun deleteTiles() = withContext(Dispatchers.IO) {
        context?.externalCacheDirs?.forEach { it.purge() }
    }
    private suspend fun unhideQuests() = withContext(Dispatchers.IO) {
        osmQuestController.unhideAll() + osmNoteQuestController.unhideAll() + otherSourceQuestController.unhideAll()
    }

    private fun getQuestPreferenceSummary(): String {
        val presetName = questPresetsSource.selectedQuestPresetName ?: getString(R.string.quest_presets_default_name)
        val hasCustomPresets = questPresetsSource.getAll().isNotEmpty()
        val presetStr = if (hasCustomPresets) getString(R.string.pref_subtitle_quests_preset_name, presetName) + "\n" else ""

        val enabledCount = questTypeRegistry.count { visibleQuestTypeSource.isVisible(it) }
        val totalCount = questTypeRegistry.size
        val enabledStr = getString(R.string.pref_subtitle_quests, enabledCount, totalCount)

        return presetStr + enabledStr
    }
}

private const val GPX_REQUEST_CODE = 387532
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

private const val LAST_KNOWN_DB_VERSION = 6L // TODO: adjust this once the version changes and handle changes

private const val BACKUP_HIDDEN_OSM_QUESTS = "quests"
private const val BACKUP_HIDDEN_NOTES = "notes"
private const val BACKUP_HIDDEN_OTHER_QUESTS = "other_source_quests"
private const val BACKUP_PRESETS = "presets"
private const val BACKUP_PRESETS_ORDERS = "orders"
private const val BACKUP_PRESETS_VISIBILITIES = "visibilities"
