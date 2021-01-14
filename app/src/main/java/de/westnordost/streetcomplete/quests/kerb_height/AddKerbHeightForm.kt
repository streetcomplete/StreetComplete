package de.westnordost.streetcomplete.quests.kerb_height

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.quests.kerb_height.KerbHeight.*
import de.westnordost.streetcomplete.view.image_select.Item

class AddKerbHeightForm : AImageListQuestAnswerFragment<KerbHeight, KerbHeight>() {

    override val items = listOf(
        Item(RAISED, R.drawable.kerb_height_raised, R.string.quest_kerb_height_raised),
        Item(LOWERED, R.drawable.kerb_height_lowered, R.string.quest_kerb_height_lowered),
        Item(FLUSH, R.drawable.kerb_height_flush, R.string.quest_kerb_height_flush),
        Item(KERB_RAMP, R.drawable.kerb_height_lowered_ramp, R.string.quest_kerb_height_lowered_ramp)
    )

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_kerb_height_answer_no_kerb) { applyAnswer(NO_KERB) }
    )

    override val itemsPerRow = 2
    override val moveFavoritesToFront = false

    override fun onClickOk(selectedItems: List<KerbHeight>) {
        applyAnswer(selectedItems.single())
    }
}
