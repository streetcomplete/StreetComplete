package de.westnordost.streetcomplete.quests.fire_hydrant_position

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.fire_hydrant_position.FireHydrantPosition.GREEN
import de.westnordost.streetcomplete.quests.fire_hydrant_position.FireHydrantPosition.LANE
import de.westnordost.streetcomplete.quests.fire_hydrant_position.FireHydrantPosition.PARKING_LOT
import de.westnordost.streetcomplete.quests.fire_hydrant_position.FireHydrantPosition.SIDEWALK
import de.westnordost.streetcomplete.view.image_select.Item

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
