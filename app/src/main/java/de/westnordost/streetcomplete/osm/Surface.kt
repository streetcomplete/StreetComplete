package de.westnordost.streetcomplete.osm

import android.util.Log
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.surface.asItem
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.Item

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
    // https://forum.openstreetmap.org/viewtopic.php?id=61042
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
    GROUND_AREA("ground");

    companion object {
        val surfaceReplacements: Map<String, String?> = mapOf(
            // that is intended for presentation of data
            // not for automatic bot replacements
            "cobblestone" to null,
            "earth" to "dirt",
            "paving_stones:30" to "paving_stones",
            "soil" to "dirt",
            "trail" to null,
            "bricks" to "paving_stones",
            "cobblestone:flattened" to  "sett",
            "brick" to "paving_stones",
        )
    }
}

fun createSurfaceStatus(tags: Map<String, String>): Surface? {
    if ("surface" !in tags) {
        return null
    }
    var surface = tags["surface"]
    if(surface in Surface.surfaceReplacements) {
        surface = Surface.surfaceReplacements[surface]
    }
    return Surface.values().find { it.osmValue == surface }
}

val ANYTHING_UNPAVED = setOf(
    "unpaved", "compacted", "gravel", "fine_gravel", "pebblestone", "grass_paver",
    "ground", "earth", "dirt", "grass", "sand", "mud", "ice", "salt", "snow", "woodchips"
)

val ANYTHING_PAVED = setOf(
    "paved", "asphalt", "cobblestone", "cobblestone:flattened", "sett",
    "concrete", "concrete:lanes", "concrete:plates", "paving_stones",
    "metal", "wood", "unhewn_cobblestone"
)

val Surface.titleResId: Int get() = when (this) {
    Surface.ASPHALT -> R.string.quest_surface_value_asphalt
    Surface.CONCRETE -> R.string.quest_surface_value_concrete
    Surface.CONCRETE_PLATES -> R.string.quest_surface_value_concrete_plates
    Surface.CONCRETE_LANES -> R.string.quest_surface_value_concrete_lanes
    Surface.FINE_GRAVEL -> R.string.quest_surface_value_fine_gravel
    Surface.PAVING_STONES -> R.string.quest_surface_value_paving_stones
    Surface.COMPACTED -> R.string.quest_surface_value_compacted
    Surface.DIRT -> R.string.quest_surface_value_dirt
    Surface.SETT -> R.string.quest_surface_value_sett
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
    Surface.PAVING_STONES -> R.drawable.surface_paving_stones
    Surface.COMPACTED -> R.drawable.surface_compacted
    Surface.DIRT -> R.drawable.surface_dirt
    Surface.SETT -> R.drawable.surface_sett
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
