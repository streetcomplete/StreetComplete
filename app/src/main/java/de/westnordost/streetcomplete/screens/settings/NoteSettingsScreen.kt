package de.westnordost.streetcomplete.screens.settings

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestController
import de.westnordost.streetcomplete.data.osmnotes.notequests.getRawBlockList
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.common.dialogs.TextInputDialog
import de.westnordost.streetcomplete.ui.common.settings.Preference
import de.westnordost.streetcomplete.ui.common.settings.SwitchPreference
import de.westnordost.streetcomplete.util.ktx.getActivity
import de.westnordost.streetcomplete.util.ktx.toast
import kotlinx.serialization.json.Json
import org.koin.compose.koinInject
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Composable
fun NoteSettingsScreen(
    onClickBack: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.pref_screen_notes)) },
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
            var showHideNotesDialog by remember { mutableStateOf(false) }
            val ctx = LocalContext.current
            SwitchPreference(
                name = stringResource(R.string.pref_show_gpx_button_title),
                description = stringResource(R.string.pref_show_gpx_button_summary),
                pref = Prefs.GPX_BUTTON,
                default = false,
            )
            SwitchPreference(
                name = stringResource(R.string.pref_swap_gpx_note_button),
                pref = Prefs.SWAP_GPX_NOTE_BUTTONS,
                default = false,
            )
            SwitchPreference(
                name = stringResource(R.string.pref_hide_keyboard_title),
                description = stringResource(R.string.pref_hide_keyboard_summary),
                pref = Prefs.HIDE_KEYBOARD_FOR_NOTE,
                default = true,
            )
            SwitchPreference(
                name = stringResource(R.string.pref_really_all_notes_title),
                description = stringResource(R.string.pref_really_all_notes_summary),
                pref = Prefs.REALLY_ALL_NOTES,
                default = false,
            )
            SwitchPreference(
                name = stringResource(R.string.pref_create_custom_quest_title),
                description = stringResource(R.string.pref_create_custom_quest_summary),
                pref = Prefs.CREATE_EXTERNAL_QUESTS,
                default = false,
            )
            SwitchPreference(
                name = stringResource(R.string.pref_save_photos_title),
                description = stringResource(R.string.pref_save_photos_summary),
                pref = Prefs.QUEST_SETTINGS_PER_PRESET,
                default = false,
            )
            Preference(
                name = stringResource(R.string.pref_hide_notes_title),
                onClick = { showHideNotesDialog = true },
            )
            val gpxLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode != Activity.RESULT_OK || it.data == null)
                    return@rememberLauncherForActivityResult
                val uri = it.data?.data ?: return@rememberLauncherForActivityResult
                val output = ctx.getActivity()?.contentResolver?.openOutputStream(uri) ?: return@rememberLauncherForActivityResult
                val os = output.buffered()
                try {
                    // read gpx and extract images
                    val filesDir = ctx.getExternalFilesDir(null)
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

                    // write files to zip
                    val zipStream = ZipOutputStream(os)
                    files.forEach {
                        val fileStream = FileInputStream(it).buffered()
                        zipStream.putNextEntry(ZipEntry(it.name))
                        fileStream.copyTo(zipStream, 1024)
                        fileStream.close()
                        zipStream.closeEntry()
                    }
                    zipStream.close()
                    files.forEach { it.delete() }
                } catch (e: IOException) {
                    ctx.toast(ctx.getString(R.string.pref_save_file_error), Toast.LENGTH_LONG)
                }
                os.close()
                output.close()
            }
            Preference(
                name = stringResource(R.string.pref_save_gpx),
                onClick = {
                    if (File(ctx.getExternalFilesDir(null), "notes.gpx").exists()) {
                        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            putExtra(Intent.EXTRA_TITLE, "notes.zip")
                            type = "application/zip"
                        }
                        gpxLauncher.launch(intent)
                    } else {
                        ctx.toast(ctx.getString(R.string.pref_files_not_found), Toast.LENGTH_LONG)
                    }
                },
            )
            val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode != Activity.RESULT_OK || it.data == null)
                    return@rememberLauncherForActivityResult
                val uri = it.data?.data ?: return@rememberLauncherForActivityResult
                val output = ctx.getActivity()?.contentResolver?.openOutputStream(uri) ?: return@rememberLauncherForActivityResult
                val os = output.buffered()
                try {
                    val filesDir = ctx.getExternalFilesDir(null)
                    val files = mutableListOf<File>()
                    val picturesDir = File(filesDir, "full_photos")
                    // get all files in pictures dir
                    if (picturesDir.isDirectory) {
                        picturesDir.walk().forEach {
                            if (!it.isDirectory) files.add(it)
                        }
                    }
                    else { // we checked for this, but better be sure
                        ctx.toast(ctx.getString(R.string.pref_files_not_found), Toast.LENGTH_LONG)
                        return@rememberLauncherForActivityResult
                    }

                    // write files to zip
                    val zipStream = ZipOutputStream(os)
                    files.forEach {
                        val fileStream = FileInputStream(it).buffered()
                        zipStream.putNextEntry(ZipEntry(it.name))
                        fileStream.copyTo(zipStream, 1024)
                        fileStream.close()
                        zipStream.closeEntry()
                    }
                    zipStream.close()
                    files.forEach { it.delete() }
                } catch (e: IOException) {
                    ctx.toast(ctx.getString(R.string.pref_save_file_error), Toast.LENGTH_LONG)
                }
                os.close()
                output.close()
            }
            Preference(
                name = stringResource(R.string.pref_get_photos_title),
                onClick = {
                    val dir = File(ctx.getExternalFilesDir(null), "full_photos")
                    if (dir.exists() && dir.isDirectory && dir.list()?.isNotEmpty() == true) {
                        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            putExtra(Intent.EXTRA_TITLE, "full_photos.zip")
                            type = "application/zip"
                        }
                        photoLauncher.launch(intent)
                    } else {
                        ctx.toast(ctx.getString(R.string.pref_files_not_found), Toast.LENGTH_LONG)
                    }
                },
            )
            if (showHideNotesDialog) {
                val prefs: Preferences = koinInject()
                val blockList = getRawBlockList(prefs)
                TextInputDialog(
                    onDismissRequest = { showHideNotesDialog = false },
                    onConfirmed = {
                        val content = it.split(",").map { it.trim().lowercase() }
                        prefs.putString(Prefs.HIDE_NOTES_BY_USERS, Json.encodeToString(content))
                        OsmQuestController.reloadQuestTypes()
                    },
                    singleLine = false,
                    title = { Text(stringResource(R.string.pref_hide_notes_message)) },
                    textInputLabel = { Text(stringResource(R.string.pref_hide_notes_hint)) },
                    text = blockList.joinToString(", ")
                )
            }
        }
    }
}
