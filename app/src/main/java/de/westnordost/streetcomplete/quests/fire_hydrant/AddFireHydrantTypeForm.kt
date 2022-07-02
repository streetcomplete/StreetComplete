package de.westnordost.streetcomplete.quests.fire_hydrant

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.fire_hydrant.FireHydrantType.PILLAR
import de.westnordost.streetcomplete.quests.fire_hydrant.FireHydrantType.POND
import de.westnordost.streetcomplete.quests.fire_hydrant.FireHydrantType.UNDERGROUND
import de.westnordost.streetcomplete.quests.fire_hydrant.FireHydrantType.WALL
import de.westnordost.streetcomplete.view.image_select.Item

class AddFireHydrantTypeForm : AImageListQuestForm<FireHydrantType, FireHydrantType>() {

    override val items = listOf(
        Item(PILLAR, R.drawable.fire_hydrant_pillar, R.string.quest_fireHydrant_type_pillar),
        Item(UNDERGROUND, R.drawable.fire_hydrant_underground, R.string.quest_fireHydrant_type_underground),
        Item(WALL, R.drawable.fire_hydrant_wall, R.string.quest_fireHydrant_type_wall),
        Item(POND, R.drawable.fire_hydrant_pond, R.string.quest_fireHydrant_type_pond)
    )

    override val itemsPerRow = 2

    override fun onClickOk(selectedItems: List<FireHydrantType>) {
        applyAnswer(selectedItems.single())
    }
}
