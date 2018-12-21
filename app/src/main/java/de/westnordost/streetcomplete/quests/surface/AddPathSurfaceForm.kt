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

class AddPathSurfaceForm : GroupedImageListQuestAnswerFragment() {

    override fun getTopItems() =
        when (val pathType = determinePathType(osmElement.tags)) {
            "bridleway" -> arrayOf(
                DIRT, GRASS, SAND,
                PEBBLES, FINE_GRAVEL, COMPACTED
            )
            "path" -> arrayOf(
                DIRT, PEBBLES, COMPACTED,
                ASPHALT, FINE_GRAVEL, PAVING_STONES
            )
            "footway" -> arrayOf(
                PAVING_STONES, ASPHALT, CONCRETE,
                COMPACTED, FINE_GRAVEL, DIRT
            )
            "cycleway" -> arrayOf(
                PAVING_STONES, ASPHALT, CONCRETE,
                COMPACTED, WOOD, METAL
            )
            "steps" -> arrayOf(
                PAVING_STONES, ASPHALT, CONCRETE,
                WOOD, SETT, UNHEWN_COBBLESTONE
            )
            else -> throw IllegalStateException("Unexpected path type $pathType")
        }

    override fun getAllItems() = arrayOf(
        // except for different panorama images, should be the same as for the road quest, to avoid confusion
        Item("paved", R.drawable.panorama_path_surface_paved, R.string.quest_surface_value_paved, arrayOf(
            ASPHALT, CONCRETE, PAVING_STONES,
            SETT, UNHEWN_COBBLESTONE, GRASS_PAVER,
            WOOD, METAL
        )),
        Item("unpaved", R.drawable.panorama_path_surface_unpaved, R.string.quest_surface_value_unpaved, arrayOf(
            COMPACTED, FINE_GRAVEL, GRAVEL,
            PEBBLES
        )),
        Item("ground",R.drawable.panorama_surface_ground, R.string.quest_surface_value_ground, arrayOf(
            DIRT, GRASS, SAND
        ))
    )

    private fun determinePathType(tags: Map<String, String>): String? {
        val pathType = tags["highway"]
        // interpet paths with foot/bicycle/horse=designated as...
        if ("path" == pathType) {
            if ("designated" == tags["bicycle"]) return "cycleway"
            if ("designated" == tags["horse"]) return "bridleway"
            if ("designated" == tags["foot"]) return "footway"
        }
        return pathType
    }
}
