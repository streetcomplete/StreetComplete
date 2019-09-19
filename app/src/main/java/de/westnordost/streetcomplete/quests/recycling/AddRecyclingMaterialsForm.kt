package de.westnordost.streetcomplete.quests.recycling


import android.os.Bundle

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.view.Item

class AddRecyclingMaterialsForm : AImageListQuestAnswerFragment<String, List<String>>() {

    override val items get() = listOf(
            Item("recycling:batteries",             R.drawable.ic_recycling_batteries,           R.string.quest_recycling_type_batteries),
            Item("recycling:cans",                  R.drawable.ic_recycling_cans,                R.string.quest_recycling_type_cans),
            Item("recycling:green_waste",           R.drawable.ic_recycling_garden_waste,        R.string.quest_recycling_type_green_waste),
            Item("recycling:glass_bottles",         R.drawable.ic_recycling_glass_bottles,       R.string.quest_recycling_type_glass_bottles),
            Item("recycling:paper",                 R.drawable.ic_recycling_paper,               R.string.quest_recycling_type_paper),
            Item("recycling:plastic",               R.drawable.ic_recycling_plastic,             R.string.quest_recycling_type_plastic),
            Item("recycling:clothes",               R.drawable.ic_recycling_clothes,             R.string.quest_recycling_type_clothes),
            Item("recycling:shoes",                 R.drawable.ic_recycling_shoes,               R.string.quest_recycling_type_shoes),
            Item("recycling:electric_appliances",   R.drawable.ic_recycling_electric_appliances, R.string.quest_recycling_type_electric_appliances)
            // see https://github.com/westnordost/StreetComplete/issues/223#issuecomment-533256767 for missing ones
    )

    override val maxSelectableItems = -1
    override val maxNumberOfInitiallyShownItems = 8

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_icon_select_with_label_below

    }

    override fun onClickOk(selectedItems: List<String>) {
        applyAnswer(selectedItems)
    }
}
