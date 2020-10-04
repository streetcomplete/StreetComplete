package de.westnordost.streetcomplete.quests.steps_ramp

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.view.image_select.Item

import de.westnordost.streetcomplete.quests.steps_ramp.StepsRamp.*

class AddStepsRampForm : AImageListQuestAnswerFragment<StepsRamp, StepsRamp>() {

    override val items = listOf(
        Item(NONE,       R.drawable.ramp_none,       R.string.quest_steps_ramp_none),
        Item(BICYCLE,    R.drawable.ramp_bicycle,    R.string.quest_steps_ramp_bicycle),
        Item(STROLLER,   R.drawable.ramp_stroller,   R.string.quest_steps_ramp_stroller),
        Item(WHEELCHAIR, R.drawable.ramp_wheelchair, R.string.quest_steps_ramp_wheelchair)
    )

    override val itemsPerRow = 2
    override val maxSelectableItems = 1

    override fun onClickOk(selectedItems: List<StepsRamp>) {
        applyAnswer(selectedItems.first())
    }
}
