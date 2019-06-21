package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.Item

enum class Surface(val item:Item<String>) {
    ASPHALT       (Item("asphalt",        R.drawable.surface_asphalt,       R.string.quest_surface_value_asphalt)),
    CONCRETE      (Item("concrete",       R.drawable.surface_concrete,      R.string.quest_surface_value_concrete)),
    FINE_GRAVEL   (Item("fine_gravel",    R.drawable.surface_fine_gravel,   R.string.quest_surface_value_fine_gravel)),
    PAVING_STONES (Item("paving_stones",  R.drawable.surface_paving_stones, R.string.quest_surface_value_paving_stones)),
    COMPACTED     (Item("compacted",      R.drawable.surface_compacted,     R.string.quest_surface_value_compacted)),
    DIRT          (Item("dirt",           R.drawable.surface_dirt,          R.string.quest_surface_value_dirt)),
    SETT          (Item("sett",           R.drawable.surface_sett,          R.string.quest_surface_value_sett)),
    // https://forum.openstreetmap.org/viewtopic.php?id=61042
    UNHEWN_COBBLESTONE (Item("unhewn_cobblestone", R.drawable.surface_cobblestone, R.string.quest_surface_value_unhewn_cobblestone)),
    GRASS_PAVER   (Item("grass_paver",    R.drawable.surface_grass_paver,   R.string.quest_surface_value_grass_paver)),
    WOOD          (Item("wood",           R.drawable.surface_wood,          R.string.quest_surface_value_wood)),
    METAL         (Item("metal",          R.drawable.surface_metal,         R.string.quest_surface_value_metal)),
    GRAVEL        (Item("gravel",         R.drawable.surface_gravel,        R.string.quest_surface_value_gravel)),
    PEBBLES       (Item("pebblestone",    R.drawable.surface_pebblestone,   R.string.quest_surface_value_pebblestone)),
    GRASS         (Item("grass",          R.drawable.surface_grass,         R.string.quest_surface_value_grass)),
    SAND          (Item("sand",           R.drawable.surface_sand,          R.string.quest_surface_value_sand));
}

fun List<Surface>.toItems() = this.map { it.item }
