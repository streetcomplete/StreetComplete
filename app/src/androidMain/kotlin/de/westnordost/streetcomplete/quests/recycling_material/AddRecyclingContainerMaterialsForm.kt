package de.westnordost.streetcomplete.quests.recycling_material

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.semantics.Role
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.ui.common.image_select.ImageListItem
import de.westnordost.streetcomplete.ui.common.image_select.SelectableIconCell
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog
import de.westnordost.streetcomplete.view.image_select.Item

// todo: Move to AImageListQuestComposeForm once it can handle the changes
class AddRecyclingContainerMaterialsForm :
    AImageListQuestForm<List<RecyclingMaterial>, RecyclingContainerMaterialsAnswer>() {

    override val descriptionResId = R.string.quest_recycling_materials_note

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_recycling_materials_answer_waste) { confirmJustTrash() }
    )

    override val items get() = RecyclingMaterial.selectableValues.map { it.asItem() }

    override val maxSelectableItems = -1

    override val itemContent =
        @Composable { item: ImageListItem<List<RecyclingMaterial>>, index: Int, onClick: () -> Unit, role: Role ->
            key(item.item) {
                SelectableIconCell(
                    item = item.item,
                    isSelected = item.checked,
                    onClick = {
                        val value = item.item.value!!
                        if (value in RecyclingMaterial.selectablePlasticValues) {
                            showPickItemForItemAtIndexDialog(
                                index,
                                RecyclingMaterial.selectablePlasticValues.map { it.asItem() })
                        } else if (isAnyGlassRecyclable && value in RecyclingMaterial.selectableGlassValues) {
                            showPickItemForItemAtIndexDialog(
                                index,
                                RecyclingMaterial.selectableGlassValues.map { it.asItem() })
                        }
                        onClick()
                    },
                    role = role
                )
            }
        }

    private val isAnyGlassRecyclable get() = countryInfo.isUsuallyAnyGlassRecyclableInContainers

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private fun showPickItemForItemAtIndexDialog(index: Int, items: List<Item<List<RecyclingMaterial>>>) {
        val ctx = context ?: return
        ImageListPickerDialog(
            ctx,
            items,
            R.layout.cell_icon_select_with_label_below,
            3
        ) { selected ->
            val newList = currentItems.value.toMutableList()
            newList[index] = ImageListItem(selected, true)
            currentItems.value = newList
        }.show()
    }

    override fun onClickOk(selectedItems: List<List<RecyclingMaterial>>) {
        applyAnswer(RecyclingMaterials(selectedItems.flatten()))
    }

    private fun confirmJustTrash() {
        activity?.let { AlertDialog.Builder(it)
            .setMessage(R.string.quest_recycling_materials_answer_waste_description)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(IsWasteContainer) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
        }
    }
}
