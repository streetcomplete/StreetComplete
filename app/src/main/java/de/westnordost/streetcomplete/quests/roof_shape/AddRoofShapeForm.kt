package de.westnordost.streetcomplete.quests.roof_shape

import android.os.Bundle

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.roof_shape.RoofShape.*
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.view.image_select.Item

class AddRoofShapeForm : AImageListQuestAnswerFragment<RoofShape, RoofShape>() {

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_roofShape_answer_many) { applyAnswer(MANY) }
    )

    override val items = listOf(
        Item(GABLED, R.drawable.ic_roof_gabled),
        Item(HIPPED, R.drawable.ic_roof_hipped),
        Item(FLAT, R.drawable.ic_roof_flat),
        Item(PYRAMIDAL, R.drawable.ic_roof_pyramidal),

        Item(HALF_HIPPED, R.drawable.ic_roof_half_hipped),
        Item(SKILLION, R.drawable.ic_roof_skillion),
        Item(GAMBREL, R.drawable.ic_roof_gambrel),
        Item(ROUND, R.drawable.ic_roof_round),

        Item(DOUBLE_SALTBOX, R.drawable.ic_roof_double_saltbox),
        Item(SALTBOX, R.drawable.ic_roof_saltbox),
        Item(MANSARD, R.drawable.ic_roof_mansard),
        Item(DOME, R.drawable.ic_roof_dome),

        Item(QUADRUPLE_SALTBOX, R.drawable.ic_roof_quadruple_saltbox),
        Item(ROUND_GABLED, R.drawable.ic_roof_round_gabled),
        Item(ONION, R.drawable.ic_roof_onion),
        Item(CONE, R.drawable.ic_roof_cone)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_labeled_icon_select
    }

    override fun onClickOk(selectedItems: List<RoofShape>) {
        applyAnswer(selectedItems.single())
    }
}
