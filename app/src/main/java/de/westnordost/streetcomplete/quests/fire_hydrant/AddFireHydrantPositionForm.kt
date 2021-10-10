package de.westnordost.streetcomplete.quests.fire_hydrant

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.view.image_select.Item
import de.westnordost.streetcomplete.quests.fire_hydrant.FireHydrantPosition.*

class AddFireHydrantPositionForm : AImageListQuestAnswerFragment<FireHydrantPosition, FireHydrantPosition>() {

    override val items = listOf(
        Item(GREEN, R.drawable.fire_hydrant_pillar, R.string.quest_fireHydrant_position_green),
        Item(LANE, R.drawable.fire_hydrant_underground, R.string.quest_fireHydrant_position_lane),
        Item(SIDEWALK, R.drawable.fire_hydrant_wall, R.string.quest_fireHydrant_position_sidewalk),
        Item(PARKING_LOT, R.drawable.fire_hydrant_pond, R.string.quest_fireHydrant_position_parking_lot)
    )

    override val itemsPerRow = 2

    override fun onClickOk(selectedItems: List<FireHydrantPosition>) {
        applyAnswer(selectedItems.single())
    }
}
