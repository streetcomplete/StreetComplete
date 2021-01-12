package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.surface.Surface.*
import de.westnordost.streetcomplete.view.image_select.Item

fun Surface.asItem(): Item<Surface> {
    return Item(this, getIconResId(), getTitleResId())
}

fun List<Surface>.toItems() = this.map { it.asItem() }

private fun Surface.getIconResId(): Int = when(this) {
    ASPHALT -> R.drawable.surface_asphalt
    CONCRETE -> R.drawable.surface_concrete
    FINE_GRAVEL -> R.drawable.surface_fine_gravel
    PAVING_STONES -> R.drawable.surface_paving_stones
    COMPACTED -> R.drawable.surface_compacted
    DIRT -> R.drawable.surface_dirt
    SETT -> R.drawable.surface_sett
    UNHEWN_COBBLESTONE -> R.drawable.surface_cobblestone
    GRASS_PAVER -> R.drawable.surface_grass_paver
    WOOD -> R.drawable.surface_wood
    METAL -> R.drawable.surface_metal
    GRAVEL -> R.drawable.surface_gravel
    PEBBLES -> R.drawable.surface_pebblestone
    GRASS -> R.drawable.surface_grass
    SAND -> R.drawable.surface_sand
    ROCK -> R.drawable.surface_rock
    CLAY -> R.drawable.surface_tennis_clay
    ARTIFICIAL_TURF -> R.drawable.surface_artificial_turf
    TARTAN -> R.drawable.surface_tartan
    PAVED -> R.drawable.path_surface_paved
    UNPAVED -> R.drawable.path_surface_unpaved
    GROUND -> R.drawable.surface_ground
}

private fun Surface.getTitleResId(): Int = when(this) {
    ASPHALT -> R.string.quest_surface_value_asphalt
    CONCRETE -> R.string.quest_surface_value_concrete
    FINE_GRAVEL -> R.string.quest_surface_value_fine_gravel
    PAVING_STONES -> R.string.quest_surface_value_paving_stones
    COMPACTED -> R.string.quest_surface_value_compacted
    DIRT -> R.string.quest_surface_value_dirt
    SETT -> R.string.quest_surface_value_sett
    UNHEWN_COBBLESTONE -> R.string.quest_surface_value_unhewn_cobblestone
    GRASS_PAVER -> R.string.quest_surface_value_grass_paver
    WOOD -> R.string.quest_surface_value_wood
    METAL -> R.string.quest_surface_value_metal
    GRAVEL -> R.string.quest_surface_value_gravel
    PEBBLES -> R.string.quest_surface_value_pebblestone
    GRASS -> R.string.quest_surface_value_grass
    SAND -> R.string.quest_surface_value_sand
    ROCK -> R.string.quest_surface_value_rock
    CLAY -> R.string.quest_surface_value_clay
    ARTIFICIAL_TURF -> R.string.quest_surface_value_artificial_turf
    TARTAN -> R.string.quest_surface_value_tartan
    PAVED -> R.string.quest_surface_value_paved
    UNPAVED -> R.string.quest_surface_value_unpaved
    GROUND -> R.string.quest_surface_value_ground
}
