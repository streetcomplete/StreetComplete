package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.surface.Surface.*
import de.westnordost.streetcomplete.view.image_select.Item

fun List<Surface>.toItems() = this.map { it.asItem() }

fun Surface.asItem(): Item<Surface> = when (this) {
    ASPHALT -> Item(this, R.drawable.surface_asphalt, R.string.quest_surface_value_asphalt)
    CONCRETE -> Item(this, R.drawable.surface_concrete, R.string.quest_surface_value_concrete)
    FINE_GRAVEL -> Item(this, R.drawable.surface_fine_gravel, R.string.quest_surface_value_fine_gravel)
    PAVING_STONES -> Item(this, R.drawable.surface_paving_stones, R.string.quest_surface_value_paving_stones)
    COMPACTED -> Item(this, R.drawable.surface_compacted, R.string.quest_surface_value_compacted)
    DIRT -> Item(this, R.drawable.surface_dirt, R.string.quest_surface_value_dirt)
    SETT -> Item(this, R.drawable.surface_sett, R.string.quest_surface_value_sett)
    UNHEWN_COBBLESTONE -> Item(this, R.drawable.surface_cobblestone, R.string.quest_surface_value_unhewn_cobblestone)
    GRASS_PAVER -> Item(this, R.drawable.surface_grass_paver, R.string.quest_surface_value_grass_paver)
    WOOD -> Item(this, R.drawable.surface_wood, R.string.quest_surface_value_wood)
    WOODCHIPS -> Item(this, R.drawable.surface_woodchips, R.string.quest_surface_value_woodchips)
    METAL -> Item(this, R.drawable.surface_metal, R.string.quest_surface_value_metal)
    GRAVEL -> Item(this, R.drawable.surface_gravel, R.string.quest_surface_value_gravel)
    PEBBLES -> Item(this, R.drawable.surface_pebblestone, R.string.quest_surface_value_pebblestone)
    GRASS -> Item(this, R.drawable.surface_grass, R.string.quest_surface_value_grass)
    SAND -> Item(this, R.drawable.surface_sand, R.string.quest_surface_value_sand)
    ROCK -> Item(this, R.drawable.surface_rock, R.string.quest_surface_value_rock)
    CLAY -> Item(this, R.drawable.surface_tennis_clay, R.string.quest_surface_value_clay)
    ARTIFICIAL_TURF -> Item(this, R.drawable.surface_artificial_turf, R.string.quest_surface_value_artificial_turf)
    TARTAN -> Item(this, R.drawable.surface_tartan, R.string.quest_surface_value_tartan)
    PAVED -> Item(this, R.drawable.path_surface_paved, R.string.quest_surface_value_paved)
    UNPAVED -> Item(this, R.drawable.path_surface_unpaved, R.string.quest_surface_value_unpaved)
    GROUND -> Item(this, R.drawable.surface_ground, R.string.quest_surface_value_ground)
}
