package de.westnordost.streetcomplete.quests.custom

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuest
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuestType
import de.westnordost.streetcomplete.util.ktx.getActivity
import kotlinx.io.IOException
import java.io.File

class CustomQuest(private val customQuestList: CustomQuestList) : ExternalSourceQuestType {

    override val changesetComment = "Edit user-defined list of elements"
    override val wikiLink = "Tags"
    override val icon = R.drawable.ic_quest_custom
    override val defaultDisabledMessage = R.string.quest_custom_quest_message

    override fun getTitle(tags: Map<String, String>): Int = R.string.quest_custom_quest_title

    override val source: String = "custom"

    override suspend fun download(bbox: BoundingBox) = getQuests(bbox)

    override var downloadEnabled = true // it's not actually a download, so no need to ever disable

    override suspend fun upload() { customQuestList.deleteSolved() }

    override fun getQuests(bbox: BoundingBox): Collection<ExternalSourceQuest> = customQuestList.get(bbox)

    override fun get(id: String): ExternalSourceQuest? = customQuestList.getQuest(id)

    override fun onAddedEdit(edit: ElementEdit, id: String) = customQuestList.markSolved(id)

    override fun onDeletedEdit(edit: ElementEdit, id: String?) {
        if (edit.isSynced) return // if it's a real undo, can't undelete the line any more
        id?.let { customQuestList.markSolved(it, false) }
    }

    override fun onSyncedEdit(edit: ElementEdit, id: String?) {
        id?.let { customQuestList.markSolved(it) } // just mark as solved, and bunch-delete in the end
    }

    override fun onSyncEditFailed(edit: ElementEdit, id: String?) {
        id?.let { customQuestList.markSolved(it, false) }
    }

    override suspend fun onUpload(edit: ElementEdit, id: String?): Boolean = true

    override fun deleteQuest(id: String): Boolean = customQuestList.delete(id)

    override fun deleteMetadataOlderThan(timestamp: Long) { }

    override val hasQuestSettings: Boolean = true

    @Composable
    override fun QuestSettings(context: Context, onDismissRequest: () -> Unit) {
        val file = File(context.getExternalFilesDir(null), FILENAME_CUSTOM_QUEST)
        val activity = LocalContext.current.getActivity()!!
        val importIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*" // can't select text file if setting to application/text
        }
        val exportIntent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_TITLE, FILENAME_CUSTOM_QUEST)
            type = "application/text"
        }
        val importFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK || it.data == null)
                return@rememberLauncherForActivityResult
            val uri = it.data?.data ?: return@rememberLauncherForActivityResult
            readFromUriToExternalFile(uri, file.name, activity)
            onDismissRequest()
        }
        val exportFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK || it.data == null)
                return@rememberLauncherForActivityResult
            val uri = it.data?.data ?: return@rememberLauncherForActivityResult
            writeFromExternalFileToUri(file.name, uri, activity)
            onDismissRequest()
        }
        AlertDialog(
            onDismissRequest = onDismissRequest,
            buttons = {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton({ importFileLauncher.launch(importIntent) }) {
                        Text(stringResource(R.string.tree_custom_quest_import))
                    }
                    if (file.exists())
                        TextButton({ exportFileLauncher.launch(exportIntent) }) {
                            Text(stringResource(R.string.tree_custom_quest_export))
                        }
                    TextButton(onDismissRequest) { Text(stringResource(android.R.string.cancel)) }
                }
            },
            title = { Text(stringResource(R.string.pref_custom_title)) },
            text = { Text(stringResource(R.string.tree_custom_quest_import_export_message)) }
        )
    }

    // todo: don't force override any more
    override fun getQuestSettingsDialog(context: Context): AlertDialog? = null

    override fun createForm() = CustomQuestForm()
}

fun readFromUriToExternalFile(uri: Uri, filename: String, activity: Activity) {
    try {
        activity.contentResolver?.openInputStream(uri)?.use { it.bufferedReader().use { reader ->
            File(activity.getExternalFilesDir(null), filename).writeText(reader.readText())
        } }
    } catch (_: IOException) {
        AlertDialog.Builder(activity)
            .setMessage(R.string.pref_save_file_error)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }
}

fun writeFromExternalFileToUri(filename: String, uri: Uri, activity: Activity) {
    try {
        activity.contentResolver?.openOutputStream(uri)?.use { it.bufferedWriter().use { writer ->
            writer.write(File(activity.getExternalFilesDir(null), filename).readText())
        } }
    } catch (_: IOException) {
        AlertDialog.Builder(activity)
            .setMessage(R.string.pref_save_file_error)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }
}
