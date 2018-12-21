package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.GroupedImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.surface.Surface.ASPHALT
import de.westnordost.streetcomplete.quests.surface.Surface.COMPACTED
import de.westnordost.streetcomplete.quests.surface.Surface.CONCRETE
import de.westnordost.streetcomplete.quests.surface.Surface.DIRT
import de.westnordost.streetcomplete.quests.surface.Surface.FINE_GRAVEL
import de.westnordost.streetcomplete.quests.surface.Surface.GRASS
import de.westnordost.streetcomplete.quests.surface.Surface.GRASS_PAVER
import de.westnordost.streetcomplete.quests.surface.Surface.GRAVEL
import de.westnordost.streetcomplete.quests.surface.Surface.METAL
import de.westnordost.streetcomplete.quests.surface.Surface.PAVING_STONES
import de.westnordost.streetcomplete.quests.surface.Surface.PEBBLES
import de.westnordost.streetcomplete.quests.surface.Surface.SAND
import de.westnordost.streetcomplete.quests.surface.Surface.SETT
import de.westnordost.streetcomplete.quests.surface.Surface.UNHEWN_COBBLESTONE
import de.westnordost.streetcomplete.quests.surface.Surface.WOOD
import de.westnordost.streetcomplete.view.Item

class AddRoadSurfaceForm : GroupedImageListQuestAnswerFragment() {

    override fun getTopItems() =
        // tracks often have different surfaces than other roads
        if (osmElement.tags["highway"] == "track")
            arrayOf(DIRT, GRASS, PEBBLES, FINE_GRAVEL, COMPACTED, ASPHALT)
        else
            arrayOf(ASPHALT, CONCRETE, SETT, PAVING_STONES, COMPACTED, DIRT)

    override fun getAllItems() = arrayOf(
        Item("paved", R.drawable.panorama_surface_paved, R.string.quest_surface_value_paved, arrayOf(
            ASPHALT, CONCRETE, PAVING_STONES,
            SETT, UNHEWN_COBBLESTONE, GRASS_PAVER,
            WOOD, METAL
        )),
        Item("unpaved", R.drawable.panorama_surface_unpaved, R.string.quest_surface_value_unpaved, arrayOf(
            COMPACTED, FINE_GRAVEL, GRAVEL,
            PEBBLES
        )),
        Item("ground", R.drawable.panorama_surface_ground, R.string.quest_surface_value_ground, arrayOf(
            DIRT, GRASS, SAND
        ))
    )
}
