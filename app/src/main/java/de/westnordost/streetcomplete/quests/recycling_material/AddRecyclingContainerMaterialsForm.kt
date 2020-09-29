package de.westnordost.streetcomplete.quests.recycling_material

import android.os.Bundle
import androidx.appcompat.app.AlertDialog

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.view.image_select.ImageSelectAdapter
import de.westnordost.streetcomplete.view.image_select.Item
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog
import de.westnordost.streetcomplete.quests.recycling_material.RecyclingMaterial.*

class AddRecyclingContainerMaterialsForm
    : AImageListQuestAnswerFragment<RecyclingMaterial, RecyclingContainerMaterialsAnswer>() {

    override val contentLayoutResId = R.layout.quest_recycling_materials

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_recycling_materials_answer_waste) { confirmJustTrash() }
    )

    override val items = listOf(
        Item(GLASS_BOTTLES, R.drawable.ic_recycling_glass_bottles, R.string.quest_recycling_type_glass_bottles),
        Item(PAPER,         R.drawable.ic_recycling_paper,         R.string.quest_recycling_type_paper),
        Item(PLASTIC,       R.drawable.ic_recycling_plastic,       R.string.quest_recycling_type_plastic_generic),
        Item(CANS,          R.drawable.ic_recycling_cans,          R.string.quest_recycling_type_cans),
        Item(SCRAP_METAL,   R.drawable.ic_recycling_scrap_metal,   R.string.quest_recycling_type_scrap_metal),
        Item(CLOTHES,       R.drawable.ic_recycling_clothes,       R.string.quest_recycling_type_clothes),
        Item(SHOES,         R.drawable.ic_recycling_shoes,         R.string.quest_recycling_type_shoes),
        Item(SMALL_ELECTRICAL_APPLIANCES, R.drawable.ic_recycling_small_electric_appliances, R.string.quest_recycling_type_electric_appliances),
        Item(BATTERIES,     R.drawable.ic_recycling_batteries,     R.string.quest_recycling_type_batteries),
        Item(GREEN_WASTE,   R.drawable.ic_recycling_garden_waste,  R.string.quest_recycling_type_green_waste),
        Item(COOKING_OIL,   R.drawable.ic_recycling_cooking_oil,   R.string.quest_recycling_type_cooking_oil),
        Item(ENGINE_OIL,    R.drawable.ic_recycling_engine_oil,    R.string.quest_recycling_type_engine_oil)
    )

    private val plasticItems = listOf(
        Item(PLASTIC,           R.drawable.ic_recycling_plastic,           R.string.quest_recycling_type_plastic),
        Item(PLASTIC_PACKAGING, R.drawable.ic_recycling_plastic_packaging, R.string.quest_recycling_type_plastic_packaging),
        Item(PLASTIC_BOTTLES,   R.drawable.ic_recycling_plastic_bottles,   R.string.quest_recycling_type_plastic_bottles)
    )

    override val maxSelectableItems = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_icon_select_with_label_below
        imageSelector.listeners.add(object : ImageSelectAdapter.OnItemSelectionListener {
            override fun onIndexSelected(index: Int) {
                val value = imageSelector.items[index].value!!

                if (value == PLASTIC) {
                    showPickItemForItemAtIndexDialog(index, plasticItems)
                }
            }

            override fun onIndexDeselected(index: Int) {}
        })
    }

    private fun showPickItemForItemAtIndexDialog(index: Int, items: List<Item<RecyclingMaterial>>) {
        val ctx = context ?: return
        ImageListPickerDialog(ctx, items, R.layout.cell_icon_select_with_label_below, 3) { selected ->
            val newList = imageSelector.items.toMutableList()
            newList[index] = selected
            imageSelector.items = newList
        }.show()
    }

    override fun onClickOk(selectedItems: List<RecyclingMaterial>) {
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
