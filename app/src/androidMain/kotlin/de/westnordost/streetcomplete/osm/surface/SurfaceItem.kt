package de.westnordost.streetcomplete.osm.surface

import android.content.res.Resources
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.surface.Surface.*
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
    ASPHALT -> R.string.quest_surface_value_asphalt
    CONCRETE -> R.string.quest_surface_value_concrete
    CONCRETE_LANES -> R.string.quest_surface_value_concrete_lanes
    FINE_GRAVEL -> R.string.quest_surface_value_fine_gravel
    PAVING_STONES -> R.string.quest_surface_value_paving_stones
    COMPACTED -> R.string.quest_surface_value_compacted
    DIRT -> R.string.quest_surface_value_dirt
    MUD -> R.string.quest_surface_value_mud
    SETT -> R.string.quest_surface_value_sett
    UNHEWN_COBBLESTONE -> R.string.quest_surface_value_unhewn_cobblestone
    GRASS_PAVER -> R.string.quest_surface_value_grass_paver
    WOOD -> R.string.quest_surface_value_wood
    WOODCHIPS -> R.string.quest_surface_value_woodchips
    METAL -> R.string.quest_surface_value_metal
    GRAVEL -> R.string.quest_surface_value_gravel
    PEBBLES -> R.string.quest_surface_value_pebblestone
    GRASS -> R.string.quest_surface_value_grass
    SAND -> R.string.quest_surface_value_sand
    ROCK -> R.string.quest_surface_value_rock
    CLAY -> R.string.quest_surface_value_clay
    ARTIFICIAL_TURF -> R.string.quest_surface_value_artificial_turf
    RUBBER -> R.string.quest_surface_value_rubber
    ACRYLIC -> R.string.quest_surface_value_acrylic
    PAVED -> R.string.quest_surface_value_paved
    UNPAVED -> R.string.quest_surface_value_unpaved
    GROUND -> R.string.quest_surface_value_ground
    UNSUPPORTED -> R.string.unknown_surface_title
}

val Surface.iconResId: Int get() = when (this) {
    ASPHALT -> R.drawable.surface_asphalt
    CONCRETE -> R.drawable.surface_concrete
    CONCRETE_LANES -> R.drawable.surface_concrete_lanes
    FINE_GRAVEL -> R.drawable.surface_fine_gravel
    PAVING_STONES -> R.drawable.surface_paving_stones
    COMPACTED -> R.drawable.surface_compacted
    DIRT -> R.drawable.surface_dirt
    MUD -> R.drawable.surface_mud
    SETT -> R.drawable.surface_sett
    UNHEWN_COBBLESTONE -> R.drawable.surface_cobblestone
    GRASS_PAVER -> R.drawable.surface_grass_paver
    WOOD -> R.drawable.surface_wood
    WOODCHIPS -> R.drawable.surface_woodchips
    METAL -> R.drawable.surface_metal
    GRAVEL -> R.drawable.surface_gravel
    PEBBLES -> R.drawable.surface_pebblestone
    GRASS -> R.drawable.surface_grass
    SAND -> R.drawable.surface_sand
    ROCK -> R.drawable.surface_rock
    CLAY -> R.drawable.surface_tennis_clay
    ARTIFICIAL_TURF -> R.drawable.surface_artificial_turf
    RUBBER -> R.drawable.surface_tartan
    ACRYLIC -> R.drawable.surface_acrylic
    PAVED -> R.drawable.surface_paved_area
    UNPAVED -> R.drawable.surface_unpaved_area
    GROUND -> R.drawable.surface_ground_area
    UNSUPPORTED -> R.drawable.space_128dp
}
