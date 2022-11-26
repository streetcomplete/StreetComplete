package de.westnordost.streetcomplete.osm.surface

import de.westnordost.streetcomplete.R

enum class Surface(val osmValue: String) {
    ASPHALT("asphalt"),
    CONCRETE("concrete"),
    CONCRETE_PLATES("concrete:plates"),
    CONCRETE_LANES("concrete:lanes"),
    FINE_GRAVEL("fine_gravel"),
    PAVING_STONES("paving_stones"),
    COMPACTED("compacted"),
    DIRT("dirt"),
    SETT("sett"),
    UNHEWN_COBBLESTONE("unhewn_cobblestone"),
    GRASS_PAVER("grass_paver"),
    WOOD("wood"),
    WOODCHIPS("woodchips"),
    METAL("metal"),
    GRAVEL("gravel"),
    PEBBLES("pebblestone"),
    GRASS("grass"),
    SAND("sand"),
    ROCK("rock"),
    CLAY("clay"),
    ARTIFICIAL_TURF("artificial_turf"),
    TARTAN("tartan"),
    PAVED_ROAD("paved"),
    UNPAVED_ROAD("unpaved"),
    GROUND_ROAD("ground"),
    PAVED_AREA("paved"),
    UNPAVED_AREA("unpaved"),
    GROUND_AREA("ground"),

    // extra values, recording duplicates
    MUD("mud"), // valid value, but not displayed specially in StreetComplete
    EARTH("earth"), // synonym of "dirt", maybe more clear
    // this values ideally would be removed from OpenStreetMap, but while they remain
    // we want to handle them somehow
    SOIL("soil"),
    PAVING_STONES_WITH_WEIRD_SUFFIX("paving_stones:30"),
    COBLLESTONE_FLATTENED("cobblestone:flattened"),
    BRICK("brick"),
    BRICKS("bricks");
}

val Surface.titleResId: Int get() = when (this) {
    Surface.ASPHALT -> R.string.quest_surface_value_asphalt
    Surface.CONCRETE -> R.string.quest_surface_value_concrete
    Surface.CONCRETE_PLATES -> R.string.quest_surface_value_concrete_plates
    Surface.CONCRETE_LANES -> R.string.quest_surface_value_concrete_lanes
    Surface.FINE_GRAVEL -> R.string.quest_surface_value_fine_gravel
    Surface.PAVING_STONES, Surface.PAVING_STONES_WITH_WEIRD_SUFFIX, Surface.BRICK, Surface.BRICKS -> R.string.quest_surface_value_paving_stones
    Surface.COMPACTED -> R.string.quest_surface_value_compacted
    Surface.DIRT, Surface.SOIL, Surface.EARTH, Surface.MUD -> R.string.quest_surface_value_dirt
    Surface.SETT, Surface.COBLLESTONE_FLATTENED -> R.string.quest_surface_value_sett
    Surface.UNHEWN_COBBLESTONE -> R.string.quest_surface_value_unhewn_cobblestone
    Surface.GRASS_PAVER -> R.string.quest_surface_value_grass_paver
    Surface.WOOD -> R.string.quest_surface_value_wood
    Surface.WOODCHIPS -> R.string.quest_surface_value_woodchips
    Surface.METAL -> R.string.quest_surface_value_metal
    Surface.GRAVEL -> R.string.quest_surface_value_gravel
    Surface.PEBBLES -> R.string.quest_surface_value_pebblestone
    Surface.GRASS -> R.string.quest_surface_value_grass
    Surface.SAND -> R.string.quest_surface_value_sand
    Surface.ROCK -> R.string.quest_surface_value_rock
    Surface.CLAY -> R.string.quest_surface_value_clay
    Surface.ARTIFICIAL_TURF -> R.string.quest_surface_value_artificial_turf
    Surface.TARTAN -> R.string.quest_surface_value_tartan
    Surface.PAVED_ROAD -> R.string.quest_surface_value_paved
    Surface.UNPAVED_ROAD -> R.string.quest_surface_value_unpaved
    Surface.GROUND_ROAD -> R.string.quest_surface_value_ground
    Surface.PAVED_AREA -> R.string.quest_surface_value_paved
    Surface.UNPAVED_AREA -> R.string.quest_surface_value_unpaved
    Surface.GROUND_AREA -> R.string.quest_surface_value_ground
}

val Surface.iconResId: Int get() = when (this) {
    Surface.ASPHALT -> R.drawable.surface_asphalt
    Surface.CONCRETE -> R.drawable.surface_concrete
    Surface.CONCRETE_PLATES -> R.drawable.surface_concrete_plates
    Surface.CONCRETE_LANES -> R.drawable.surface_concrete_lanes
    Surface.FINE_GRAVEL -> R.drawable.surface_fine_gravel
    Surface.PAVING_STONES, Surface.PAVING_STONES_WITH_WEIRD_SUFFIX, Surface.BRICK, Surface.BRICKS -> R.drawable.surface_paving_stones
    Surface.COMPACTED -> R.drawable.surface_compacted
    Surface.DIRT, Surface.SOIL, Surface.EARTH, Surface.MUD -> R.drawable.surface_dirt
    Surface.SETT, Surface.COBLLESTONE_FLATTENED -> R.drawable.surface_sett
    Surface.UNHEWN_COBBLESTONE -> R.drawable.surface_cobblestone
    Surface.GRASS_PAVER -> R.drawable.surface_grass_paver
    Surface.WOOD -> R.drawable.surface_wood
    Surface.WOODCHIPS -> R.drawable.surface_woodchips
    Surface.METAL -> R.drawable.surface_metal
    Surface.GRAVEL -> R.drawable.surface_gravel
    Surface.PEBBLES -> R.drawable.surface_pebblestone
    Surface.GRASS -> R.drawable.surface_grass
    Surface.SAND -> R.drawable.surface_sand
    Surface.ROCK -> R.drawable.surface_rock
    Surface.CLAY -> R.drawable.surface_tennis_clay
    Surface.ARTIFICIAL_TURF -> R.drawable.surface_artificial_turf
    Surface.TARTAN -> R.drawable.surface_tartan
    Surface.PAVED_ROAD -> R.drawable.path_surface_paved
    Surface.UNPAVED_ROAD -> R.drawable.path_surface_unpaved
    Surface.GROUND_ROAD -> R.drawable.surface_ground
    Surface.PAVED_AREA -> R.drawable.surface_paved_area
    Surface.UNPAVED_AREA -> R.drawable.surface_unpaved_area
    Surface.GROUND_AREA -> R.drawable.surface_ground_area
}

