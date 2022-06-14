package de.westnordost.streetcomplete.quests.fire_hydrant_position

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.fire_hydrant_position.FireHydrantPosition.GREEN
import de.westnordost.streetcomplete.quests.fire_hydrant_position.FireHydrantPosition.LANE
import de.westnordost.streetcomplete.quests.fire_hydrant_position.FireHydrantPosition.PARKING_LOT
import de.westnordost.streetcomplete.quests.fire_hydrant_position.FireHydrantPosition.SIDEWALK
import de.westnordost.streetcomplete.view.image_select.Item

class AddFireHydrantPositionForm : AImageListQuestForm<FireHydrantPosition, FireHydrantPosition>() {

    override val items
        get() = if (element.tags["fire_hydrant:type"] == "pillar") {
            listOf(
                Item(GREEN, R.drawable.fire_hydrant_position_pillar_green, R.string.quest_fireHydrant_position_green),
                Item(LANE, R.drawable.fire_hydrant_position_pillar_lane, R.string.quest_fireHydrant_position_lane),
                Item(SIDEWALK, R.drawable.fire_hydrant_position_pillar_sidewalk, R.string.quest_fireHydrant_position_sidewalk),
                Item(PARKING_LOT, R.drawable.fire_hydrant_position_pillar_parking, R.string.quest_fireHydrant_position_parking_lot)
            )
        } else {
            listOf(
                Item(GREEN, R.drawable.fire_hydrant_position_underground_green, R.string.quest_fireHydrant_position_green),
                Item(LANE, R.drawable.fire_hydrant_position_underground_lane, R.string.quest_fireHydrant_position_lane),
                Item(SIDEWALK, R.drawable.fire_hydrant_position_underground_sidewalk, R.string.quest_fireHydrant_position_sidewalk),
                Item(PARKING_LOT, R.drawable.fire_hydrant_position_underground_parking, R.string.quest_fireHydrant_position_parking_lot)
            )
        }

    override val itemsPerRow = 2

    override fun onClickOk(selectedItems: List<FireHydrantPosition>) {
        applyAnswer(selectedItems.single())
    }
}
