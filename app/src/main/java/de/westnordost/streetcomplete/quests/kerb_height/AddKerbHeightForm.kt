package de.westnordost.streetcomplete.quests.kerb_height

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.view.image_select.Item

class AddKerbHeightForm : AImageListQuestAnswerFragment<String, String>() {

    override val items = listOf(
        Item("raised", R.drawable.kerb_height_raised, R.string.quest_kerb_height_raised),
        Item("lowered", R.drawable.kerb_height_lowered, R.string.quest_kerb_height_lowered),
        Item("flush", R.drawable.kerb_height_flush, R.string.quest_kerb_height_flush),
        Item("lowered", R.drawable.kerb_height_lowered_ramp, R.string.quest_kerb_height_lowered_ramp)
    )

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_kerb_height_answer_no_kerb) { applyAnswer("no") }
    )

    override val itemsPerRow = 2

    override fun onClickOk(selectedItems: List<String>) {
        applyAnswer(selectedItems.single())
    }
}
