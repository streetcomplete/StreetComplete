package de.westnordost.streetcomplete.quests.recycling_material

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.recycling_material.RecyclingMaterial.BATTERIES
import de.westnordost.streetcomplete.quests.recycling_material.RecyclingMaterial.BEVERAGE_CARTONS
import de.westnordost.streetcomplete.quests.recycling_material.RecyclingMaterial.CANS
import de.westnordost.streetcomplete.quests.recycling_material.RecyclingMaterial.CLOTHES
import de.westnordost.streetcomplete.quests.recycling_material.RecyclingMaterial.COOKING_OIL
import de.westnordost.streetcomplete.quests.recycling_material.RecyclingMaterial.ENGINE_OIL
import de.westnordost.streetcomplete.quests.recycling_material.RecyclingMaterial.GLASS
import de.westnordost.streetcomplete.quests.recycling_material.RecyclingMaterial.GLASS_BOTTLES
import de.westnordost.streetcomplete.quests.recycling_material.RecyclingMaterial.GREEN_WASTE
import de.westnordost.streetcomplete.quests.recycling_material.RecyclingMaterial.PAPER
import de.westnordost.streetcomplete.quests.recycling_material.RecyclingMaterial.PLASTIC
import de.westnordost.streetcomplete.quests.recycling_material.RecyclingMaterial.PLASTIC_BOTTLES
import de.westnordost.streetcomplete.quests.recycling_material.RecyclingMaterial.PLASTIC_PACKAGING
import de.westnordost.streetcomplete.quests.recycling_material.RecyclingMaterial.SCRAP_METAL
import de.westnordost.streetcomplete.quests.recycling_material.RecyclingMaterial.SHOES
import de.westnordost.streetcomplete.quests.recycling_material.RecyclingMaterial.SMALL_ELECTRICAL_APPLIANCES
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog
import de.westnordost.streetcomplete.view.image_select.ImageSelectAdapter
import de.westnordost.streetcomplete.view.image_select.Item

class AddRecyclingContainerMaterialsForm :
    AImageListQuestForm<List<RecyclingMaterial>, RecyclingContainerMaterialsAnswer>() {

    override val descriptionResId = R.string.quest_recycling_materials_note

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_recycling_materials_answer_waste) { confirmJustTrash() }
    )

    override val items get() = listOf(
        if (isAnyGlassRecycleable) {
            Item(listOf(GLASS), R.drawable.ic_recycling_glass, R.string.quest_recycling_type_any_glass)
        } else {
            Item(listOf(GLASS_BOTTLES), R.drawable.ic_recycling_glass_bottles, R.string.quest_recycling_type_glass_bottles)
        },
        Item(listOf(PAPER),         R.drawable.ic_recycling_paper,         R.string.quest_recycling_type_paper),
        Item(listOf(PLASTIC),       R.drawable.ic_recycling_plastic,       R.string.quest_recycling_type_plastic_generic),
        Item(listOf(CANS),          R.drawable.ic_recycling_cans,          R.string.quest_recycling_type_cans),
        Item(listOf(SCRAP_METAL),   R.drawable.ic_recycling_scrap_metal,   R.string.quest_recycling_type_scrap_metal),
        Item(listOf(CLOTHES),       R.drawable.ic_recycling_clothes,       R.string.quest_recycling_type_clothes),
        Item(listOf(SHOES),         R.drawable.ic_recycling_shoes,         R.string.quest_recycling_type_shoes),
        Item(listOf(SMALL_ELECTRICAL_APPLIANCES), R.drawable.ic_recycling_small_electrical_appliances, R.string.quest_recycling_type_electric_appliances),
        Item(listOf(BATTERIES),     R.drawable.ic_recycling_batteries,     R.string.quest_recycling_type_batteries),
        Item(listOf(GREEN_WASTE),   R.drawable.ic_recycling_garden_waste,  R.string.quest_recycling_type_green_waste),
        Item(listOf(COOKING_OIL),   R.drawable.ic_recycling_cooking_oil,   R.string.quest_recycling_type_cooking_oil),
        Item(listOf(ENGINE_OIL),    R.drawable.ic_recycling_engine_oil,    R.string.quest_recycling_type_engine_oil)
    )

    private val plasticItems = listOf(
        Item(listOf(PLASTIC),           R.drawable.ic_recycling_plastic,           R.string.quest_recycling_type_plastic),
        Item(listOf(PLASTIC_PACKAGING), R.drawable.ic_recycling_plastic_packaging, R.string.quest_recycling_type_plastic_packaging),
        Item(listOf(PLASTIC_BOTTLES, BEVERAGE_CARTONS), R.drawable.ic_recycling_plastic_bottles_and_cartons, R.string.quest_recycling_type_plastic_bottles_and_cartons),
        Item(listOf(PLASTIC_BOTTLES),   R.drawable.ic_recycling_plastic_bottles,   R.string.quest_recycling_type_plastic_bottles),
        Item(listOf(BEVERAGE_CARTONS),  R.drawable.ic_recycling_beverage_cartons,  R.string.quest_recycling_type_beverage_cartons),
    )

    override val maxSelectableItems = -1

    private val isAnyGlassRecycleable get() = countryInfo.isUsuallyAnyGlassRecyclableInContainers

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_icon_select_with_label_below
        imageSelector.listeners.add(object : ImageSelectAdapter.OnItemSelectionListener {
            override fun onIndexSelected(index: Int) {
                val value = imageSelector.items[index].value!!

                if (value in plasticItems.map { it.value!! }) {
                    showPickItemForItemAtIndexDialog(index, plasticItems)
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
