package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.Item

object Surface {
    val ASPHALT =       Item("asphalt", R.drawable.surface_asphalt, R.string.quest_surface_value_asphalt)
    val CONCRETE =      Item("concrete", R.drawable.surface_concrete, R.string.quest_surface_value_concrete)
    val FINE_GRAVEL =   Item("fine_gravel", R.drawable.surface_fine_gravel, R.string.quest_surface_value_fine_gravel)
    val PAVING_STONES = Item( "paving_stones", R.drawable.surface_paving_stones, R.string.quest_surface_value_paving_stones)
    val COMPACTED =     Item("compacted", R.drawable.surface_compacted, R.string.quest_surface_value_compacted)
    val DIRT =          Item("dirt", R.drawable.surface_dirt, R.string.quest_surface_value_dirt)
    val SETT =          Item("sett", R.drawable.surface_sett, R.string.quest_surface_value_sett)
    // https://forum.openstreetmap.org/viewtopic.php?id=61042
    val UNHEWN_COBBLESTONE = Item("unhewn_cobblestone", R.drawable.surface_cobblestone, R.string.quest_surface_value_unhewn_cobblestone)
    val GRASS_PAVER =   Item("grass_paver", R.drawable.surface_grass_paver, R.string.quest_surface_value_grass_paver)
    val WOOD =          Item("wood", R.drawable.surface_wood, R.string.quest_surface_value_wood)
    val METAL =         Item("metal", R.drawable.surface_metal, R.string.quest_surface_value_metal)
    val GRAVEL =        Item("gravel", R.drawable.surface_gravel, R.string.quest_surface_value_gravel)
    val PEBBLES =       Item("pebblestone", R.drawable.surface_pebblestone, R.string.quest_surface_value_pebblestone)
    val GRASS =         Item("grass", R.drawable.surface_grass, R.string.quest_surface_value_grass)
    val SAND =          Item("sand", R.drawable.surface_sand, R.string.quest_surface_value_sand)
}
