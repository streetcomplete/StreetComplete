package de.westnordost.streetcomplete.quests.recycling_material

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.ui.common.dialogs.QuestConfirmationDialog
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.Confirm
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.takeFavorites
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject
import kotlin.getValue

class AddRecyclingContainerMaterialsForm : AbstractOsmQuestForm<RecyclingContainerMaterialsAnswer>() {

    private val prefs: Preferences by inject()

    @Composable
    override fun Content() {
        val reorderedItems = remember { moveFavouritesToFront(RecyclingMaterial.entries) }
        var selectedItems by rememberSerializable { mutableStateOf(emptySet<RecyclingMaterial>()) }

        var confirmJustTrash by remember { mutableStateOf(false) }

        QuestForm(
            answers = Confirm(isComplete = selectedItems.isNotEmpty()) {
                prefs.addLastPicked(this::class.simpleName!!, selectedItems.toList())
                applyAnswer(RecyclingMaterials(selectedItems))
            },
            otherAnswers = listOf(
                Answer(stringResource(Res.string.quest_recycling_materials_answer_waste)) { confirmJustTrash = true }
            )
        ) {
            RecyclingContainerMaterialsForm(
                items = reorderedItems,
                tree = RecyclingMaterial.tree,
                selectedItems = selectedItems,
                onSelectedItems = { selectedItems = it },
            )
        }

        if (confirmJustTrash) {
            QuestConfirmationDialog(
                onDismissRequest = { confirmJustTrash = false },
                onConfirmed = { applyAnswer(IsWasteContainer) },
                text = { Text(stringResource(Res.string.quest_recycling_materials_answer_waste_description)) },
            )
        }
    }

    private fun moveFavouritesToFront(originalList: List<RecyclingMaterial>): List<RecyclingMaterial> {
        val favourites = prefs
            .getLastPicked<RecyclingMaterial>(this::class.simpleName!!)
            .takeFavorites(4)
        return (favourites + originalList).distinct()
    }
}
