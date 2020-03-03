package de.westnordost.streetcomplete.quests.recycling_material

import android.os.Bundle
import androidx.appcompat.app.AlertDialog

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.view.ImageSelectAdapter
import de.westnordost.streetcomplete.view.Item
import de.westnordost.streetcomplete.view.dialogs.ImageListPickerDialog

class AddRecyclingContainerMaterialsForm : AImageListQuestAnswerFragment<String, RecyclingContainerMaterialsAnswer>() {

    override val contentLayoutResId = R.layout.quest_recycling_materials

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_recycling_materials_answer_waste) { confirmJustTrash() }
    )

    override val items = listOf(
        Item("glass_bottles",       R.drawable.ic_recycling_glass_bottles,       R.string.quest_recycling_type_glass_bottles),
        Item("paper",               R.drawable.ic_recycling_paper,               R.string.quest_recycling_type_paper),
        Item("plastic",             R.drawable.ic_recycling_plastic,             R.string.quest_recycling_type_plastic_generic),
        Item("cans",                R.drawable.ic_recycling_cans,                R.string.quest_recycling_type_cans),
        Item("scrap_metal",         R.drawable.ic_recycling_scrap_metal,         R.string.quest_recycling_type_scrap_metal),
        Item("clothes",             R.drawable.ic_recycling_clothes,             R.string.quest_recycling_type_clothes),
        Item("shoes",               R.drawable.ic_recycling_shoes,               R.string.quest_recycling_type_shoes),
        Item("electric_appliances", R.drawable.ic_recycling_electric_appliances, R.string.quest_recycling_type_electric_appliances),
        Item("batteries",           R.drawable.ic_recycling_batteries,           R.string.quest_recycling_type_batteries),
        Item("green_waste",         R.drawable.ic_recycling_garden_waste,        R.string.quest_recycling_type_green_waste)
    )

    private val plasticItems = listOf(
        Item("plastic",             R.drawable.ic_recycling_plastic,             R.string.quest_recycling_type_plastic),
        Item("plastic_packaging",   R.drawable.ic_recycling_plastic_packaging,   R.string.quest_recycling_type_plastic_packaging),
        Item("plastic_bottles",     R.drawable.ic_recycling_plastic_bottles,     R.string.quest_recycling_type_plastic_bottles)
    )

    override val maxSelectableItems = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_icon_select_with_label_below
        imageSelector.listeners.add(object : ImageSelectAdapter.OnItemSelectionListener {
            override fun onIndexSelected(index: Int) {
                val value = imageSelector.items[index].value!!

                if (plasticItems.map { it.value!! }.contains(value)) {
                    showPickItemForItemAtIndexDialog(index, plasticItems)
                }
            }

            override fun onIndexDeselected(index: Int) {}
        })
    }

    private fun showPickItemForItemAtIndexDialog(index: Int, items: List<Item<String>>) {
        val ctx = context ?: return
        ImageListPickerDialog(ctx, items, R.layout.cell_icon_select_with_label_below, 3) { selected ->
            val newList = imageSelector.items.toMutableList()
            newList[index] = selected
            imageSelector.items = newList
        }.show()
    }

    override fun onClickOk(selectedItems: List<String>) {
        applyAnswer(RecyclingMaterials(selectedItems))
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
