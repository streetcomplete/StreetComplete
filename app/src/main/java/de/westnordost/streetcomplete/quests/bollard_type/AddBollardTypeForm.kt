package de.westnordost.streetcomplete.quests.bollard_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.bollard_type.BollardType.FIXED
import de.westnordost.streetcomplete.quests.bollard_type.BollardType.FLEXIBLE
import de.westnordost.streetcomplete.quests.bollard_type.BollardType.FOLDABLE
import de.westnordost.streetcomplete.quests.bollard_type.BollardType.REMOVABLE
import de.westnordost.streetcomplete.quests.bollard_type.BollardType.RISING
import de.westnordost.streetcomplete.view.image_select.Item

class AddBollardTypeForm : AImageListQuestForm<BollardType, BollardTypeAnswer>() {

    override val items = listOf(
        Item(RISING,    R.drawable.bollard_rising,    R.string.quest_bollard_type_rising),
        Item(REMOVABLE, R.drawable.bollard_removable, R.string.quest_bollard_type_removable),
        Item(FOLDABLE,  R.drawable.bollard_foldable,  R.string.quest_bollard_type_foldable2),
        Item(FLEXIBLE,  R.drawable.bollard_flexible,  R.string.quest_bollard_type_flexible),
        Item(FIXED,     R.drawable.bollard_fixed,     R.string.quest_bollard_type_fixed),
    )

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<BollardType>) {
        applyAnswer(selectedItems.single())
    }

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_bollard_type_not_bollard) {
            applyAnswer(BarrierTypeIsNotBollard)
        },
    )
}
