package de.westnordost.streetcomplete.quests.surface

import android.content.res.Resources
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
import de.westnordost.streetcomplete.quests.surface.Surface.GROUND_AREA
import de.westnordost.streetcomplete.quests.surface.Surface.GROUND_ROAD
import de.westnordost.streetcomplete.quests.surface.Surface.METAL
import de.westnordost.streetcomplete.quests.surface.Surface.PAVED_AREA
import de.westnordost.streetcomplete.quests.surface.Surface.PAVED_ROAD
import de.westnordost.streetcomplete.quests.surface.Surface.PAVING_STONES
import de.westnordost.streetcomplete.quests.surface.Surface.PEBBLES
import de.westnordost.streetcomplete.quests.surface.Surface.ROCK
import de.westnordost.streetcomplete.quests.surface.Surface.SAND
import de.westnordost.streetcomplete.quests.surface.Surface.SETT
import de.westnordost.streetcomplete.quests.surface.Surface.TARTAN
import de.westnordost.streetcomplete.quests.surface.Surface.UNHEWN_COBBLESTONE
import de.westnordost.streetcomplete.quests.surface.Surface.UNPAVED_AREA
import de.westnordost.streetcomplete.quests.surface.Surface.UNPAVED_ROAD
import de.westnordost.streetcomplete.quests.surface.Surface.WOOD
import de.westnordost.streetcomplete.quests.surface.Surface.WOODCHIPS
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

private val Surface.titleResId: Int get() = when (this) {
    ASPHALT ->         R.string.quest_surface_value_asphalt
    CONCRETE ->        R.string.quest_surface_value_concrete
    CONCRETE_PLATES -> R.string.quest_surface_value_concrete_plates
    CONCRETE_LANES ->  R.string.quest_surface_value_concrete_lanes
    FINE_GRAVEL ->     R.string.quest_surface_value_fine_gravel
    PAVING_STONES ->   R.string.quest_surface_value_paving_stones
    COMPACTED ->       R.string.quest_surface_value_compacted
    DIRT ->            R.string.quest_surface_value_dirt
    SETT ->            R.string.quest_surface_value_sett
    UNHEWN_COBBLESTONE -> R.string.quest_surface_value_unhewn_cobblestone
    GRASS_PAVER ->     R.string.quest_surface_value_grass_paver
    WOOD ->            R.string.quest_surface_value_wood
    WOODCHIPS ->       R.string.quest_surface_value_woodchips
    METAL ->           R.string.quest_surface_value_metal
    GRAVEL ->          R.string.quest_surface_value_gravel
    PEBBLES ->         R.string.quest_surface_value_pebblestone
    GRASS ->           R.string.quest_surface_value_grass
    SAND ->            R.string.quest_surface_value_sand
    ROCK ->            R.string.quest_surface_value_rock
    CLAY ->            R.string.quest_surface_value_clay
    ARTIFICIAL_TURF -> R.string.quest_surface_value_artificial_turf
    TARTAN ->          R.string.quest_surface_value_tartan
    PAVED_ROAD ->      R.string.quest_surface_value_paved
    UNPAVED_ROAD ->    R.string.quest_surface_value_unpaved
    GROUND_ROAD ->     R.string.quest_surface_value_ground
    PAVED_AREA ->      R.string.quest_surface_value_paved
    UNPAVED_AREA ->    R.string.quest_surface_value_unpaved
    GROUND_AREA ->     R.string.quest_surface_value_ground
}

private val Surface.iconResId: Int get() = when (this) {
    ASPHALT ->         R.drawable.surface_asphalt
    CONCRETE ->        R.drawable.surface_concrete
    CONCRETE_PLATES -> R.drawable.surface_concrete_plates
    CONCRETE_LANES ->  R.drawable.surface_concrete_lanes
    FINE_GRAVEL ->     R.drawable.surface_fine_gravel
    PAVING_STONES ->   R.drawable.surface_paving_stones
    COMPACTED ->       R.drawable.surface_compacted
    DIRT ->            R.drawable.surface_dirt
    SETT ->            R.drawable.surface_sett
    UNHEWN_COBBLESTONE -> R.drawable.surface_cobblestone
    GRASS_PAVER ->     R.drawable.surface_grass_paver
    WOOD ->            R.drawable.surface_wood
    WOODCHIPS ->       R.drawable.surface_woodchips
    METAL ->           R.drawable.surface_metal
    GRAVEL ->          R.drawable.surface_gravel
    PEBBLES ->         R.drawable.surface_pebblestone
    GRASS ->           R.drawable.surface_grass
    SAND ->            R.drawable.surface_sand
    ROCK ->            R.drawable.surface_rock
    CLAY ->            R.drawable.surface_tennis_clay
    ARTIFICIAL_TURF -> R.drawable.surface_artificial_turf
    TARTAN ->          R.drawable.surface_tartan
    PAVED_ROAD ->      R.drawable.path_surface_paved
    UNPAVED_ROAD ->    R.drawable.path_surface_unpaved
    GROUND_ROAD ->     R.drawable.surface_ground
    PAVED_AREA ->      R.drawable.surface_paved_area
    UNPAVED_AREA ->    R.drawable.surface_unpaved_area
    GROUND_AREA ->     R.drawable.surface_ground_area
}
