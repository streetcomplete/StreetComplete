package de.westnordost.streetcomplete.quests.recycling_material

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cheonjaeung.compose.grid.SimpleGridCells
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.QuestConfirmationDialog
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.item_select.ItemsSelectGrid
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddRecyclingContainerMaterialsForm(
    on: (QuestAction<RecyclingContainerMaterialsAnswer>) -> Unit,
) {
    var selectedItems by rememberSerializable { mutableStateOf(emptySet<RecyclingMaterial>()) }

    var confirmJustTrash by remember { mutableStateOf(false) }

    QuestForm(
        isComplete = selectedItems.isNotEmpty(),
        onClickOk = { on(Answer(RecyclingMaterials(selectedItems))) },
        otherAnswers = { listOf(
            AnswerItem(stringResource(Res.string.quest_recycling_materials_answer_waste)) {
                confirmJustTrash = true
            }
        ) },
        on = on,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CompositionLocalProvider(
                LocalContentAlpha provides ContentAlpha.medium,
                LocalTextStyle provides MaterialTheme.typography.body2
            ) {
                Text(stringResource(Res.string.quest_recycling_materials_note))
                Text(stringResource(Res.string.quest_multiselect_hint))
            }
            ItemsSelectGrid(
                columns = SimpleGridCells.Fixed(4),
                items = RecyclingMaterial.entries,
                selectedItems = selectedItems,
                onSelect = { item, selected ->
                    // this here is the reason why it can't be just a normal ItemsSelectQuestForm:
                    // certain values are actually sub-categories of other values.
                    if (!selected) {
                        selectedItems -= item
                    } else {
                        val newSelectedItems = selectedItems.toMutableSet()
                        val tree = RecyclingMaterial.tree
                        val parentItems = tree.yieldParentValues(item).orEmpty()
                        val childItems = tree.yieldChildValues(item).orEmpty()
                        newSelectedItems.removeAll(parentItems)
                        newSelectedItems.removeAll(childItems)
                        newSelectedItems.add(item)
                        selectedItems = selectedItems
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                ImageWithLabel(painterResource(it.icon), stringResource(it.title))
            }
        }
    }

    if (confirmJustTrash) {
        QuestConfirmationDialog(
            onDismissRequest = { confirmJustTrash = false },
            onConfirmed = { on(Answer(IsWasteContainer)) },
            text = { Text(stringResource(Res.string.quest_recycling_materials_answer_waste_description)) },
        )
    }
}
