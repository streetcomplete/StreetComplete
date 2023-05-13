package de.westnordost.streetcomplete.quests.roof_colour

import android.os.Bundle
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.roof_colour.RoofColour.MANY

class AddRoofColourForm : AImageListQuestForm<RoofColour, RoofColour>() {

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_roofColour_answer_many) { applyAnswer(MANY) }
    )

    override val items get() = RoofColour.values().mapNotNull { it.asItem(requireContext()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_icon_select_with_label_below
    }

    override fun onClickOk(selectedItems: List<RoofColour>) {
        applyAnswer(selectedItems.single())
    }
}
