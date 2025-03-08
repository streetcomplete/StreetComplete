package de.westnordost.streetcomplete.quests.tree

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.custom.readFromUriToExternalFile
import de.westnordost.streetcomplete.quests.custom.writeFromExternalFileToUri
import de.westnordost.streetcomplete.util.ktx.getActivity
import java.io.File

class AddTreeGenus : OsmFilterQuestType<TreeAnswer>() {

    override val elementFilter = """
        nodes with
          natural = tree
          and !genus and !species and !taxon
          and !~"genus:.*" and !~"species:.*" and !~"taxon:.*"
    """
    override val changesetComment = "Add tree genus/species"
    override val defaultDisabledMessage = R.string.quest_tree_disabled_msg
    override val wikiLink = "Key:genus"
    override val icon = R.drawable.ic_quest_tree

    override fun getTitle(tags: Map<String, String>) = R.string.quest_tree_genus_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with natural = tree")

    override fun createForm() = AddTreeGenusForm()

    override val isDeleteElementEnabled = true

    override fun applyAnswerTo(answer: TreeAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is NotTreeButStump -> tags["natural"] = "tree_stump"
            is Tree -> {
                if (answer.isSpecies)
                    tags["species"] = answer.name
                else
                    tags["genus"] = answer.name
            }
        }
    }

    @Composable
    override fun QuestSettings(context: Context, onDismissRequest: () -> Unit) {
        val file = File(context.getExternalFilesDir(null), FILENAME_TREES)
        val activity = LocalContext.current.getActivity()!!
        val importIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*" // can't select text file if setting to application/text
        }
        val exportIntent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_TITLE, FILENAME_TREES)
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
                TextButton({ super.getQuestSettingsDialog(context)?.show(); onDismissRequest() }) {
                    Text(stringResource(R.string.element_selection_button))
                }
            },
            title = { Text(stringResource(R.string.pref_trees_title)) },
            text = { Text(stringResource(R.string.tree_custom_quest_import_export_message)) }
        )
    }
}
