package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.GroupedImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.surface.Surface.*
import de.westnordost.streetcomplete.view.Item

class AddRoadSurfaceForm : GroupedImageListQuestAnswerFragment() {

    override val topItems get() =
        // tracks often have different surfaces than other roads
        if (osmElement!!.tags["highway"] == "track")
            listOf(DIRT, GRASS, PEBBLES, FINE_GRAVEL, COMPACTED, ASPHALT)
        else
            listOf(ASPHALT, CONCRETE, SETT, PAVING_STONES, COMPACTED, DIRT)

    override val allItems = listOf(
        Item("paved", R.drawable.panorama_surface_paved, R.string.quest_surface_value_paved, 0, listOf(
            ASPHALT, CONCRETE, PAVING_STONES,
            SETT, UNHEWN_COBBLESTONE, GRASS_PAVER,
            WOOD, METAL
        )),
        Item("unpaved", R.drawable.panorama_surface_unpaved, R.string.quest_surface_value_unpaved, 0, listOf(
            COMPACTED, FINE_GRAVEL, GRAVEL,
            PEBBLES
        )),
        Item("ground", R.drawable.panorama_surface_ground, R.string.quest_surface_value_ground, 0, listOf(
            DIRT, GRASS, SAND
        ))
    )
}
