package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AGroupedImageListQuestAnswerFragment
import de.westnordost.streetcomplete.view.Item
import de.westnordost.streetcomplete.quests.surface.Surface.*

class AddRoadSurfaceForm : AGroupedImageListQuestAnswerFragment<String,String>() {

    override val topItems get() =
        // tracks often have different surfaces than other roads
        if (osmElement!!.tags["highway"] == "track")
            listOf(DIRT, GRASS, PEBBLES, FINE_GRAVEL, COMPACTED, ASPHALT).toItems()
        else
            listOf(ASPHALT, CONCRETE, SETT, PAVING_STONES, COMPACTED, DIRT).toItems()

    override val allItems = listOf(
        Item("paved", R.drawable.panorama_surface_paved, R.string.quest_surface_value_paved, null, listOf(
            ASPHALT, CONCRETE, PAVING_STONES,
            SETT, UNHEWN_COBBLESTONE, GRASS_PAVER,
            WOOD, METAL
        ).toItems()),
        Item("unpaved", R.drawable.panorama_surface_unpaved, R.string.quest_surface_value_unpaved, null, listOf(
            COMPACTED, FINE_GRAVEL, GRAVEL,
            PEBBLES
        ).toItems()),
        Item("ground", R.drawable.panorama_surface_ground, R.string.quest_surface_value_ground, null, listOf(
            DIRT, GRASS, SAND
        ).toItems())
    )

    override fun onClickOk(value: String) {
        applyAnswer(value)
    }
}
