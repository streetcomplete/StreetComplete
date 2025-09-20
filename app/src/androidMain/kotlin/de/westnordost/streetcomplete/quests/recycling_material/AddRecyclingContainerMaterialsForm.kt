package de.westnordost.streetcomplete.quests.recycling_material

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cheonjaeung.compose.grid.SimpleGridCells
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.ComposeViewBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_multiselect_hint
import de.westnordost.streetcomplete.resources.quest_recycling_materials_note
import de.westnordost.streetcomplete.ui.common.dialogs.SimpleItemSelectDialog
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.item_select.ItemsSelect
import de.westnordost.streetcomplete.ui.util.content
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

typealias RecyclingMaterialsItem = List<RecyclingMaterial>

class AddRecyclingContainerMaterialsForm : AbstractOsmQuestForm<RecyclingContainerMaterialsAnswer>() {

    override val contentLayoutResId = R.layout.compose_view
    private val binding by contentViewBinding(ComposeViewBinding::bind)
    override val defaultExpanded = false

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_recycling_materials_answer_waste) { confirmJustTrash() }
    )

    private val items = mutableStateOf(RecyclingMaterial.selectableValues.map { listOf(it) })
    private val selectedItems = mutableStateOf(emptySet<RecyclingMaterialsItem>())

    private val isAnyGlassRecyclable get() = countryInfo.isUsuallyAnyGlassRecyclableInContainers

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeViewBase.content { Surface {
            var subItems by remember { mutableStateOf<List<RecyclingMaterialsItem>?>(null) }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CompositionLocalProvider(
                    LocalContentAlpha provides ContentAlpha.medium,
                    LocalTextStyle provides MaterialTheme.typography.body2
                ) {
                    Text(stringResource(Res.string.quest_recycling_materials_note))
                    Text(stringResource(Res.string.quest_multiselect_hint))
                }
                ItemsSelect(
                    columns = SimpleGridCells.Fixed(4),
                    items = items.value,
                    selectedItems = selectedItems.value,
                    onSelect = { item: RecyclingMaterialsItem, selected: Boolean ->
                        val currentSelectedItems = selectedItems.value.toMutableList()
                        if (selected) {
                            if (item in RecyclingMaterial.selectablePlasticValues) {
                                subItems = RecyclingMaterial.selectablePlasticValues
                            } else if (isAnyGlassRecyclable && item in RecyclingMaterial.selectableGlassValues) {
                                subItems = RecyclingMaterial.selectableGlassValues
                            } else {
                                currentSelectedItems.add(item)
                                selectedItems.value = currentSelectedItems.toSet()
                            }
                        } else {
                            currentSelectedItems.remove(item)
                            selectedItems.value = currentSelectedItems.toSet()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ImageWithLabel(painterResource(it.icon), stringResource(it.title))
                }
            }

            subItems?.let { subItems2 ->
                SimpleItemSelectDialog(
                    onDismissRequest = { subItems = null },
                    columns = SimpleGridCells.Fixed(3),
                    items = subItems2,
                    onSelected = { item ->
                        items.value = items.value.map { if (it in subItems2) item else it }
                        val currentSelectedItems = selectedItems.value.toMutableList()
                        currentSelectedItems.add(item)
                        selectedItems.value = currentSelectedItems.toSet()
                    },
                    itemContent = { item ->
                        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
                    }
                )
            }
        } }
    }

    override fun onClickOk() {
        applyAnswer(RecyclingMaterials(selectedItems.value.flatten()))
    }

    override fun isFormComplete() = selectedItems.value.isNotEmpty()

    private fun confirmJustTrash() {
        activity?.let { AlertDialog.Builder(it)
            .setMessage(R.string.quest_recycling_materials_answer_waste_description)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(IsWasteContainer) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
        }
    }
}
