package de.westnordost.streetcomplete.quests.tracktype

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.view.Item

class AddTracktypeForm : AImageListQuestAnswerFragment<String, String>() {

    override val items = listOf(
        Item("grade1", R.drawable.tracktype_grade1, R.string.quest_tracktype_grade1),
        Item("grade2", R.drawable.tracktype_grade2, R.string.quest_tracktype_grade2),
        Item("grade3", R.drawable.tracktype_grade3, R.string.quest_tracktype_grade3),
        Item("grade4", R.drawable.tracktype_grade4, R.string.quest_tracktype_grade4),
        Item("grade5", R.drawable.tracktype_grade5, R.string.quest_tracktype_grade5)
    )

    override val itemsPerRow = 3

    override val moveFavoritesToFront = false

    override fun onClickOk(selectedItems: List<String>) {
        applyAnswer(selectedItems.single())
    }
}
