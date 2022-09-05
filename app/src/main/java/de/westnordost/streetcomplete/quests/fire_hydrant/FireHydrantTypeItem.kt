package de.westnordost.streetcomplete.quests.fire_hydrant

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.fire_hydrant.FireHydrantType.PILLAR
import de.westnordost.streetcomplete.quests.fire_hydrant.FireHydrantType.POND
import de.westnordost.streetcomplete.quests.fire_hydrant.FireHydrantType.UNDERGROUND
import de.westnordost.streetcomplete.quests.fire_hydrant.FireHydrantType.WALL
import de.westnordost.streetcomplete.view.image_select.Item

fun FireHydrantType.asItem() = Item(this, iconResId, titleResId)

private val FireHydrantType.titleResId: Int get() = when (this) {
    PILLAR ->      R.string.quest_fireHydrant_type_pillar
    UNDERGROUND -> R.string.quest_fireHydrant_type_underground
    WALL ->        R.string.quest_fireHydrant_type_wall
    POND ->        R.string.quest_fireHydrant_type_pond
}

private val FireHydrantType.iconResId: Int get() = when (this) {
    PILLAR ->      R.drawable.fire_hydrant_pillar
    UNDERGROUND -> R.drawable.fire_hydrant_underground
    WALL ->        R.drawable.fire_hydrant_wall
    POND ->        R.drawable.fire_hydrant_pond
}
