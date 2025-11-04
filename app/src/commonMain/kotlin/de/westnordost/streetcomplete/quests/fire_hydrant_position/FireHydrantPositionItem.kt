package de.westnordost.streetcomplete.quests.fire_hydrant_position

import de.westnordost.streetcomplete.quests.fire_hydrant_position.FireHydrantPosition.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.fire_hydrant_position_pillar_green
import de.westnordost.streetcomplete.resources.fire_hydrant_position_pillar_lane
import de.westnordost.streetcomplete.resources.fire_hydrant_position_pillar_parking
import de.westnordost.streetcomplete.resources.fire_hydrant_position_pillar_sidewalk
import de.westnordost.streetcomplete.resources.fire_hydrant_position_underground_green
import de.westnordost.streetcomplete.resources.fire_hydrant_position_underground_lane
import de.westnordost.streetcomplete.resources.fire_hydrant_position_underground_parking
import de.westnordost.streetcomplete.resources.fire_hydrant_position_underground_sidewalk
import de.westnordost.streetcomplete.resources.quest_fireHydrant_position_green
import de.westnordost.streetcomplete.resources.quest_fireHydrant_position_lane
import de.westnordost.streetcomplete.resources.quest_fireHydrant_position_parking_lot
import de.westnordost.streetcomplete.resources.quest_fireHydrant_position_sidewalk
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val FireHydrantPosition.title: StringResource get() = when (this) {
    GREEN ->       Res.string.quest_fireHydrant_position_green
    LANE ->        Res.string.quest_fireHydrant_position_lane
    SIDEWALK ->    Res.string.quest_fireHydrant_position_sidewalk
    PARKING_LOT -> Res.string.quest_fireHydrant_position_parking_lot
}

fun FireHydrantPosition.getIcon(isPillar: Boolean): DrawableResource =
    if (isPillar) pillarIcon else undergroundIcon

private val FireHydrantPosition.pillarIcon: DrawableResource get() = when (this) {
    GREEN ->       Res.drawable.fire_hydrant_position_pillar_green
    LANE ->        Res.drawable.fire_hydrant_position_pillar_lane
    SIDEWALK ->    Res.drawable.fire_hydrant_position_pillar_sidewalk
    PARKING_LOT -> Res.drawable.fire_hydrant_position_pillar_parking
}

private val FireHydrantPosition.undergroundIcon: DrawableResource get() = when (this) {
    GREEN ->       Res.drawable.fire_hydrant_position_underground_green
    LANE ->        Res.drawable.fire_hydrant_position_underground_lane
    SIDEWALK ->    Res.drawable.fire_hydrant_position_underground_sidewalk
    PARKING_LOT -> Res.drawable.fire_hydrant_position_underground_parking
}
