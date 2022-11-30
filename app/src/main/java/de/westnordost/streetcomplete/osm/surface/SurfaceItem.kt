package de.westnordost.streetcomplete.osm.surface

import android.content.res.Resources
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.DrawableImage
import de.westnordost.streetcomplete.view.ResImage
import de.westnordost.streetcomplete.view.ResText
import de.westnordost.streetcomplete.view.RotatedCircleDrawable
import de.westnordost.streetcomplete.view.controller.StreetSideDisplayItem
import de.westnordost.streetcomplete.view.controller.StreetSideItem2
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.Item

fun List<Surface>.toItems() = this.map { it.asItem() }

fun Surface.asItem(): DisplayItem<Surface> = Item(this, iconResId, titleResId)

fun Surface.asStreetSideItem(resources: Resources): StreetSideDisplayItem<Surface> =
    StreetSideItem2(
        this,
        ResImage(R.drawable.ic_sidewalk_illustration_yes),
        ResText(titleResId),
        ResImage(iconResId),
        DrawableImage(RotatedCircleDrawable(resources.getDrawable(iconResId)))
    )

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
