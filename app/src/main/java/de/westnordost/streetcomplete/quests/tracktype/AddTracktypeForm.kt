package de.westnordost.streetcomplete.quests.tracktype

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.tracktype.Tracktype.GRADE1
import de.westnordost.streetcomplete.quests.tracktype.Tracktype.GRADE2
import de.westnordost.streetcomplete.quests.tracktype.Tracktype.GRADE3
import de.westnordost.streetcomplete.quests.tracktype.Tracktype.GRADE4
import de.westnordost.streetcomplete.quests.tracktype.Tracktype.GRADE5
import de.westnordost.streetcomplete.view.image_select.Item

class AddTracktypeForm : AImageListQuestForm<Tracktype, Tracktype>() {

    override val items = listOf(
        Item(GRADE1, R.drawable.tracktype_grade1, R.string.quest_tracktype_grade1),
        Item(GRADE2, R.drawable.tracktype_grade2, R.string.quest_tracktype_grade2a),
        Item(GRADE3, R.drawable.tracktype_grade3, R.string.quest_tracktype_grade3a),
        Item(GRADE4, R.drawable.tracktype_grade4, R.string.quest_tracktype_grade4),
        Item(GRADE5, R.drawable.tracktype_grade5, R.string.quest_tracktype_grade5)
    )

    override val itemsPerRow = 3

    override val moveFavoritesToFront = false

    override fun onClickOk(selectedItems: List<Tracktype>) {
        applyAnswer(selectedItems.single())
    }
}
