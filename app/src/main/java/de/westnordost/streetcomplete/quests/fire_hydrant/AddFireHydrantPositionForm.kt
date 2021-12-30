package de.westnordost.streetcomplete.quests.fire_hydrant

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.view.image_select.Item
import de.westnordost.streetcomplete.quests.fire_hydrant.FireHydrantPosition.*

class AddFireHydrantPositionForm : AImageListQuestAnswerFragment<FireHydrantPosition, FireHydrantPosition>() {

    override val items = listOf(
        Item(GREEN, R.drawable.fire_hydrant_position_green, R.string.quest_fireHydrant_position_green),
        Item(LANE, R.drawable.fire_hydrant_position_lane, R.string.quest_fireHydrant_position_lane),
        Item(SIDEWALK, R.drawable.fire_hydrant_position_sidewalk, R.string.quest_fireHydrant_position_sidewalk),
        Item(PARKING_LOT, R.drawable.fire_hydrant_position_parking, R.string.quest_fireHydrant_position_parking_lot)
    )

    override val itemsPerRow = 2

    override fun onClickOk(selectedItems: List<FireHydrantPosition>) {
        applyAnswer(selectedItems.single())
    }
}
