package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.GroupedImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.surface.Surface.*
import de.westnordost.streetcomplete.view.Item

class AddPathSurfaceForm : GroupedImageListQuestAnswerFragment() {

    override val topItems get() =
        when (val pathType = determinePathType(osmElement!!.tags)) {
            "bridleway" -> listOf(
                DIRT, GRASS, SAND,
                PEBBLES, FINE_GRAVEL, COMPACTED
            )
            "path" -> listOf(
                DIRT, PEBBLES, COMPACTED,
                ASPHALT, FINE_GRAVEL, PAVING_STONES
            )
            "footway" -> listOf(
                PAVING_STONES, ASPHALT, CONCRETE,
                COMPACTED, FINE_GRAVEL, DIRT
            )
            "cycleway" -> listOf(
                PAVING_STONES, ASPHALT, CONCRETE,
                COMPACTED, WOOD, METAL
            )
            "steps" -> listOf(
                PAVING_STONES, ASPHALT, CONCRETE,
                WOOD, SETT, UNHEWN_COBBLESTONE
            )
            else -> throw IllegalStateException("Unexpected path type $pathType")
        }

    override val allItems = listOf(
        // except for different panorama images, should be the same as for the road quest, to avoid confusion
        Item("paved", R.drawable.panorama_path_surface_paved, R.string.quest_surface_value_paved, 0, listOf(
            ASPHALT, CONCRETE, PAVING_STONES,
            SETT, UNHEWN_COBBLESTONE, GRASS_PAVER,
            WOOD, METAL
        )),
        Item("unpaved", R.drawable.panorama_path_surface_unpaved, R.string.quest_surface_value_unpaved, 0, listOf(
            COMPACTED, FINE_GRAVEL, GRAVEL,
            PEBBLES
        )),
        Item("ground",R.drawable.panorama_surface_ground, R.string.quest_surface_value_ground, 0, listOf(
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
