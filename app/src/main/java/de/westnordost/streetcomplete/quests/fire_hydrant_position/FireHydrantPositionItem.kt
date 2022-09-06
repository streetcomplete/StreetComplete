package de.westnordost.streetcomplete.quests.fire_hydrant_position

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.fire_hydrant_position.FireHydrantPosition.GREEN
import de.westnordost.streetcomplete.quests.fire_hydrant_position.FireHydrantPosition.LANE
import de.westnordost.streetcomplete.quests.fire_hydrant_position.FireHydrantPosition.PARKING_LOT
import de.westnordost.streetcomplete.quests.fire_hydrant_position.FireHydrantPosition.SIDEWALK
import de.westnordost.streetcomplete.view.image_select.Item

fun FireHydrantPosition.asItem(isPillar: Boolean) =
    Item(this, if (isPillar) pillarIconResId else undergroundIconResId, titleResId)

private val FireHydrantPosition.titleResId: Int get() = when (this) {
    GREEN ->       R.string.quest_fireHydrant_position_green
    LANE ->        R.string.quest_fireHydrant_position_lane
    SIDEWALK ->    R.string.quest_fireHydrant_position_sidewalk
    PARKING_LOT -> R.string.quest_fireHydrant_position_parking_lot
}

private val FireHydrantPosition.pillarIconResId: Int get() = when (this) {
    GREEN ->       R.drawable.fire_hydrant_position_pillar_green
    LANE ->        R.drawable.fire_hydrant_position_pillar_lane
    SIDEWALK ->    R.drawable.fire_hydrant_position_pillar_sidewalk
    PARKING_LOT -> R.drawable.fire_hydrant_position_pillar_parking
}

private val FireHydrantPosition.undergroundIconResId: Int get() = when (this) {
    GREEN ->       R.drawable.fire_hydrant_position_underground_green
    LANE ->        R.drawable.fire_hydrant_position_underground_lane
    SIDEWALK ->    R.drawable.fire_hydrant_position_underground_sidewalk
    PARKING_LOT -> R.drawable.fire_hydrant_position_underground_parking
}
