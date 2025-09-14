package de.westnordost.streetcomplete.quests.recycling_material

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AItemsSelectQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem

class AddRecyclingContainerMaterialsForm :
    AItemsSelectQuestForm<List<RecyclingMaterial>, RecyclingContainerMaterialsAnswer>() {

    override val descriptionResId = R.string.quest_recycling_materials_note

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_recycling_materials_answer_waste) { confirmJustTrash() }
    )

    override val items = RecyclingMaterial.selectableValues

    private val isAnyGlassRecyclable get() = countryInfo.isUsuallyAnyGlassRecyclableInContainers

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.listeners.add(object : ImageSelectAdapter.OnItemSelectionListener {
            override fun onIndexSelected(index: Int) {
                val value = imageSelector.items[index].value!!

                if (value in RecyclingMaterial.selectablePlasticValues) {
                    showPickItemForItemAtIndexDialog(index, RecyclingMaterial.selectablePlasticValues.map { it.asItem() })
                } else if (isAnyGlassRecyclable && value in RecyclingMaterial.selectableGlassValues) {
                    showPickItemForItemAtIndexDialog(index, RecyclingMaterial.selectableGlassValues.map { it.asItem() })
                }
            }

            override fun onIndexDeselected(index: Int) {}
        })
    }

    private fun showPickItemForItemAtIndexDialog(index: Int, items: List<Item<List<RecyclingMaterial>>>) {
        val ctx = context ?: return
        ImageListPickerDialog(ctx, items, R.layout.cell_icon_select_with_label_below, 3) { selected ->
            val newList = imageSelector.items.toMutableList()
            newList[index] = selected
            imageSelector.items = newList
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
