package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.surface.Surface.ARTIFICIAL_TURF
import de.westnordost.streetcomplete.quests.surface.Surface.ASPHALT
import de.westnordost.streetcomplete.quests.surface.Surface.CLAY
import de.westnordost.streetcomplete.quests.surface.Surface.COMPACTED
import de.westnordost.streetcomplete.quests.surface.Surface.CONCRETE
import de.westnordost.streetcomplete.quests.surface.Surface.CONCRETE_LANES
import de.westnordost.streetcomplete.quests.surface.Surface.CONCRETE_PLATES
import de.westnordost.streetcomplete.quests.surface.Surface.DIRT
import de.westnordost.streetcomplete.quests.surface.Surface.FINE_GRAVEL
import de.westnordost.streetcomplete.quests.surface.Surface.GRASS
import de.westnordost.streetcomplete.quests.surface.Surface.GRASS_PAVER
import de.westnordost.streetcomplete.quests.surface.Surface.GRAVEL
import de.westnordost.streetcomplete.quests.surface.Surface.GROUND
import de.westnordost.streetcomplete.quests.surface.Surface.METAL
import de.westnordost.streetcomplete.quests.surface.Surface.PAVED
import de.westnordost.streetcomplete.quests.surface.Surface.PAVING_STONES
import de.westnordost.streetcomplete.quests.surface.Surface.PEBBLES
import de.westnordost.streetcomplete.quests.surface.Surface.ROCK
import de.westnordost.streetcomplete.quests.surface.Surface.SAND
import de.westnordost.streetcomplete.quests.surface.Surface.SETT
import de.westnordost.streetcomplete.quests.surface.Surface.TARTAN
import de.westnordost.streetcomplete.quests.surface.Surface.UNHEWN_COBBLESTONE
import de.westnordost.streetcomplete.quests.surface.Surface.UNPAVED
import de.westnordost.streetcomplete.quests.surface.Surface.WOOD
import de.westnordost.streetcomplete.quests.surface.Surface.WOODCHIPS
import de.westnordost.streetcomplete.view.image_select.Item

fun List<Surface>.toItems() = this.map { it.asItem() }

fun Surface.asItem(): Item<Surface> = when (this) {
    ASPHALT -> Item(this, R.drawable.surface_asphalt, R.string.quest_surface_value_asphalt)
    CONCRETE -> Item(this, R.drawable.surface_concrete, R.string.quest_surface_value_concrete)
    CONCRETE_PLATES -> Item(this, R.drawable.surface_concrete_plates, R.string.quest_surface_value_concrete_plates)
    CONCRETE_LANES -> Item(this, R.drawable.surface_concrete_lanes, R.string.quest_surface_value_concrete_lanes)
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
