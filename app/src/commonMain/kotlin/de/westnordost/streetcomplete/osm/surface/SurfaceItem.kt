package de.westnordost.streetcomplete.osm.surface

import de.westnordost.streetcomplete.osm.surface.Surface.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_surface_value_acrylic
import de.westnordost.streetcomplete.resources.quest_surface_value_artificial_turf
import de.westnordost.streetcomplete.resources.quest_surface_value_asphalt
import de.westnordost.streetcomplete.resources.quest_surface_value_clay
import de.westnordost.streetcomplete.resources.quest_surface_value_compacted
import de.westnordost.streetcomplete.resources.quest_surface_value_concrete
import de.westnordost.streetcomplete.resources.quest_surface_value_concrete_lanes
import de.westnordost.streetcomplete.resources.quest_surface_value_dirt
import de.westnordost.streetcomplete.resources.quest_surface_value_fine_gravel
import de.westnordost.streetcomplete.resources.quest_surface_value_grass
import de.westnordost.streetcomplete.resources.quest_surface_value_grass_paver
import de.westnordost.streetcomplete.resources.quest_surface_value_gravel
import de.westnordost.streetcomplete.resources.quest_surface_value_ground
import de.westnordost.streetcomplete.resources.quest_surface_value_metal
import de.westnordost.streetcomplete.resources.quest_surface_value_mud
import de.westnordost.streetcomplete.resources.quest_surface_value_paved
import de.westnordost.streetcomplete.resources.quest_surface_value_paving_stones
import de.westnordost.streetcomplete.resources.quest_surface_value_pebblestone
import de.westnordost.streetcomplete.resources.quest_surface_value_rock
import de.westnordost.streetcomplete.resources.quest_surface_value_rubber
import de.westnordost.streetcomplete.resources.quest_surface_value_sand
import de.westnordost.streetcomplete.resources.quest_surface_value_sett
import de.westnordost.streetcomplete.resources.quest_surface_value_unhewn_cobblestone
import de.westnordost.streetcomplete.resources.quest_surface_value_unpaved
import de.westnordost.streetcomplete.resources.quest_surface_value_wood
import de.westnordost.streetcomplete.resources.quest_surface_value_woodchips
import de.westnordost.streetcomplete.resources.surface_acrylic
import de.westnordost.streetcomplete.resources.surface_artificial_turf
import de.westnordost.streetcomplete.resources.surface_asphalt
import de.westnordost.streetcomplete.resources.surface_cobblestone
import de.westnordost.streetcomplete.resources.surface_compacted
import de.westnordost.streetcomplete.resources.surface_concrete
import de.westnordost.streetcomplete.resources.surface_concrete_lanes
import de.westnordost.streetcomplete.resources.surface_dirt
import de.westnordost.streetcomplete.resources.surface_fine_gravel
import de.westnordost.streetcomplete.resources.surface_grass
import de.westnordost.streetcomplete.resources.surface_grass_paver
import de.westnordost.streetcomplete.resources.surface_gravel
import de.westnordost.streetcomplete.resources.surface_ground_area
import de.westnordost.streetcomplete.resources.surface_metal
import de.westnordost.streetcomplete.resources.surface_mud
import de.westnordost.streetcomplete.resources.surface_paved_area
import de.westnordost.streetcomplete.resources.surface_paving_stones
import de.westnordost.streetcomplete.resources.surface_pebblestone
import de.westnordost.streetcomplete.resources.surface_rock
import de.westnordost.streetcomplete.resources.surface_sand
import de.westnordost.streetcomplete.resources.surface_sett
import de.westnordost.streetcomplete.resources.surface_tartan
import de.westnordost.streetcomplete.resources.surface_tennis_clay
import de.westnordost.streetcomplete.resources.surface_unpaved_area
import de.westnordost.streetcomplete.resources.surface_wood
import de.westnordost.streetcomplete.resources.surface_woodchips
import de.westnordost.streetcomplete.resources.unknown_surface_title
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val Surface.title: StringResource get() = when (this) {
    ASPHALT -> Res.string.quest_surface_value_asphalt
    CONCRETE -> Res.string.quest_surface_value_concrete
    CONCRETE_LANES -> Res.string.quest_surface_value_concrete_lanes
    FINE_GRAVEL -> Res.string.quest_surface_value_fine_gravel
    PAVING_STONES -> Res.string.quest_surface_value_paving_stones
    COMPACTED -> Res.string.quest_surface_value_compacted
    DIRT -> Res.string.quest_surface_value_dirt
    MUD -> Res.string.quest_surface_value_mud
    SETT -> Res.string.quest_surface_value_sett
    UNHEWN_COBBLESTONE -> Res.string.quest_surface_value_unhewn_cobblestone
    GRASS_PAVER -> Res.string.quest_surface_value_grass_paver
    WOOD -> Res.string.quest_surface_value_wood
    WOODCHIPS -> Res.string.quest_surface_value_woodchips
    METAL -> Res.string.quest_surface_value_metal
    GRAVEL -> Res.string.quest_surface_value_gravel
    PEBBLES -> Res.string.quest_surface_value_pebblestone
    GRASS -> Res.string.quest_surface_value_grass
    SAND -> Res.string.quest_surface_value_sand
    ROCK -> Res.string.quest_surface_value_rock
    CLAY -> Res.string.quest_surface_value_clay
    ARTIFICIAL_TURF -> Res.string.quest_surface_value_artificial_turf
    RUBBER -> Res.string.quest_surface_value_rubber
    ACRYLIC -> Res.string.quest_surface_value_acrylic
    PAVED -> Res.string.quest_surface_value_paved
    UNPAVED -> Res.string.quest_surface_value_unpaved
    GROUND -> Res.string.quest_surface_value_ground
    UNSUPPORTED -> Res.string.unknown_surface_title
}

val Surface.icon: DrawableResource? get() = when (this) {
    ASPHALT -> Res.drawable.surface_asphalt
    CONCRETE -> Res.drawable.surface_concrete
    CONCRETE_LANES -> Res.drawable.surface_concrete_lanes
    FINE_GRAVEL -> Res.drawable.surface_fine_gravel
    PAVING_STONES -> Res.drawable.surface_paving_stones
    COMPACTED -> Res.drawable.surface_compacted
    DIRT -> Res.drawable.surface_dirt
    MUD -> Res.drawable.surface_mud
    SETT -> Res.drawable.surface_sett
    UNHEWN_COBBLESTONE -> Res.drawable.surface_cobblestone
    GRASS_PAVER -> Res.drawable.surface_grass_paver
    WOOD -> Res.drawable.surface_wood
    WOODCHIPS -> Res.drawable.surface_woodchips
    METAL -> Res.drawable.surface_metal
    GRAVEL -> Res.drawable.surface_gravel
    PEBBLES -> Res.drawable.surface_pebblestone
    GRASS -> Res.drawable.surface_grass
    SAND -> Res.drawable.surface_sand
    ROCK -> Res.drawable.surface_rock
    CLAY -> Res.drawable.surface_tennis_clay
    ARTIFICIAL_TURF -> Res.drawable.surface_artificial_turf
    RUBBER -> Res.drawable.surface_tartan
    ACRYLIC -> Res.drawable.surface_acrylic
    PAVED -> Res.drawable.surface_paved_area
    UNPAVED -> Res.drawable.surface_unpaved_area
    GROUND -> Res.drawable.surface_ground_area
    UNSUPPORTED -> null
}
