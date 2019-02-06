package de.westnordost.streetcomplete.quests.roof_shape

import android.os.Bundle

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.view.Item

class AddRoofShapeForm : AImageListQuestAnswerFragment<String, String>() {

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_roofShape_answer_many) { applyAnswer("many") }
    )

    override val items = listOf(
        Item("gabled", R.drawable.ic_roof_gabled),
        Item("hipped", R.drawable.ic_roof_hipped),
        Item("flat", R.drawable.ic_roof_flat),
        Item("pyramidal", R.drawable.ic_roof_pyramidal),

        Item("half-hipped", R.drawable.ic_roof_half_hipped),
        Item("skillion", R.drawable.ic_roof_skillion),
        Item("gambrel", R.drawable.ic_roof_gambrel),
        Item("round", R.drawable.ic_roof_round),

        Item("double_saltbox", R.drawable.ic_roof_double_saltbox),
        Item("saltbox", R.drawable.ic_roof_saltbox),
        Item("mansard", R.drawable.ic_roof_mansard),
        Item("dome", R.drawable.ic_roof_dome),

        Item("quadruple_saltbox", R.drawable.ic_roof_quadruple_saltbox),
        Item("round_gabled", R.drawable.ic_roof_round_gabled),
        Item("onion", R.drawable.ic_roof_onion),
        Item("cone", R.drawable.ic_roof_cone)
    )

    override val maxNumberOfInitiallyShownItems = 8

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_labeled_icon_select
    }

    override fun onClickOk(selectedItems: List<String>) {
        applyAnswer(selectedItems.single())
    }
}
