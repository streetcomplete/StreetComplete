package de.westnordost.streetcomplete.quests.fire_hydrant

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.fire_hydrant.FireHydrantType.*
import de.westnordost.streetcomplete.view.image_select.Item

class AddFireHydrantTypeForm : AImageListQuestAnswerFragment<FireHydrantType, FireHydrantType>() {

    override val items = listOf(
        Item(PILLAR, R.drawable.fire_hydrant_pillar, R.string.quest_fireHydrant_type_pillar),
        Item(UNDERGROUND, R.drawable.fire_hydrant_underground, R.string.quest_fireHydrant_type_underground),
        Item(WALL, R.drawable.fire_hydrant_wall, R.string.quest_fireHydrant_type_wall),
        Item(POND, R.drawable.fire_hydrant_pond, R.string.quest_fireHydrant_type_pond)
    )

    override val itemsPerRow = 2

    override suspend fun addInitialMapMarkers() {
        getMapData().filter("nodes with emergency = fire_hydrant").forEach {
            putMarker(it, R.drawable.ic_pin_fire_hydrant)
        }
    }

    override fun onClickOk(selectedItems: List<FireHydrantType>) {
        applyAnswer(selectedItems.single())
    }
}
