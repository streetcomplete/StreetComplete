package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.GroupedItem
import de.westnordost.streetcomplete.view.Item

enum class Surface(
    override val value: String,
    override val drawableId: Int,
    override val titleId: Int)
    : GroupedItem {

    ASPHALT      ("asphalt",        R.drawable.surface_asphalt,       R.string.quest_surface_value_asphalt),
    CONCRETE     ("concrete",       R.drawable.surface_concrete,      R.string.quest_surface_value_concrete),
    FINE_GRAVEL  ("fine_gravel",    R.drawable.surface_fine_gravel,   R.string.quest_surface_value_fine_gravel),
    PAVING_STONES("paving_stones",  R.drawable.surface_paving_stones, R.string.quest_surface_value_paving_stones),
    COMPACTED    ("compacted",      R.drawable.surface_compacted,     R.string.quest_surface_value_compacted),
    DIRT         ("dirt",           R.drawable.surface_dirt,          R.string.quest_surface_value_dirt),
    SETT         ("sett",           R.drawable.surface_sett,          R.string.quest_surface_value_sett),
    // https://forum.openstreetmap.org/viewtopic.php?id=61042
    UNHEWN_COBBLESTONE("unhewn_cobblestone", R.drawable.surface_cobblestone, R.string.quest_surface_value_unhewn_cobblestone),
    GRASS_PAVER  ("grass_paver",    R.drawable.surface_grass_paver,   R.string.quest_surface_value_grass_paver),
    WOOD         ("wood",           R.drawable.surface_wood,          R.string.quest_surface_value_wood),
    METAL        ("metal",          R.drawable.surface_metal,         R.string.quest_surface_value_metal),
    GRAVEL       ("gravel",         R.drawable.surface_gravel,        R.string.quest_surface_value_gravel),
    PEBBLES      ("pebblestone",    R.drawable.surface_pebblestone,   R.string.quest_surface_value_pebblestone),
    GRASS        ("grass",          R.drawable.surface_grass,         R.string.quest_surface_value_grass),
    SAND         ("sand",           R.drawable.surface_sand,          R.string.quest_surface_value_sand);
}
