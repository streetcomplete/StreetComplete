package de.westnordost.streetcomplete.overlays.surface

import android.util.Log
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.Surface
import de.westnordost.streetcomplete.osm.Surface.ARTIFICIAL_TURF
import de.westnordost.streetcomplete.osm.Surface.ASPHALT
import de.westnordost.streetcomplete.osm.Surface.CLAY
import de.westnordost.streetcomplete.osm.Surface.COMPACTED
import de.westnordost.streetcomplete.osm.Surface.CONCRETE
import de.westnordost.streetcomplete.osm.Surface.CONCRETE_LANES
import de.westnordost.streetcomplete.osm.Surface.CONCRETE_PLATES
import de.westnordost.streetcomplete.osm.Surface.DIRT
import de.westnordost.streetcomplete.osm.Surface.FINE_GRAVEL
import de.westnordost.streetcomplete.osm.Surface.GRASS
import de.westnordost.streetcomplete.osm.Surface.GRASS_PAVER
import de.westnordost.streetcomplete.osm.Surface.GRAVEL
import de.westnordost.streetcomplete.osm.Surface.GROUND_AREA
import de.westnordost.streetcomplete.osm.Surface.GROUND_ROAD
import de.westnordost.streetcomplete.osm.Surface.METAL
import de.westnordost.streetcomplete.osm.Surface.PAVED_AREA
import de.westnordost.streetcomplete.osm.Surface.PAVED_ROAD
import de.westnordost.streetcomplete.osm.Surface.PAVING_STONES
import de.westnordost.streetcomplete.osm.Surface.PEBBLES
import de.westnordost.streetcomplete.osm.Surface.ROCK
import de.westnordost.streetcomplete.osm.Surface.SAND
import de.westnordost.streetcomplete.osm.Surface.SETT
import de.westnordost.streetcomplete.osm.Surface.TARTAN
import de.westnordost.streetcomplete.osm.Surface.UNHEWN_COBBLESTONE
import de.westnordost.streetcomplete.osm.Surface.UNPAVED_AREA
import de.westnordost.streetcomplete.osm.Surface.UNPAVED_ROAD
import de.westnordost.streetcomplete.osm.Surface.WOOD
import de.westnordost.streetcomplete.osm.Surface.WOODCHIPS
import de.westnordost.streetcomplete.osm.createSurfaceStatus
import de.westnordost.streetcomplete.osm.isPrivateOnFoot
import de.westnordost.streetcomplete.osm.titleResId
import de.westnordost.streetcomplete.overlays.Color
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.PolygonStyle
import de.westnordost.streetcomplete.overlays.PolylineStyle
import de.westnordost.streetcomplete.overlays.Style
import de.westnordost.streetcomplete.quests.surface.AddPathSurface
import de.westnordost.streetcomplete.quests.surface.AddRoadSurface
import de.westnordost.streetcomplete.quests.surface.asItem
import de.westnordost.streetcomplete.view.ResText

class SurfaceOverlay : Overlay {

    private val parentQuest = AddRoadSurface()
    override val title = R.string.overlay_surface
    override val icon = parentQuest.icon
    override val changesetComment = parentQuest.changesetComment
    override val wikiLink: String = parentQuest.wikiLink
    override val achievements = parentQuest.achievements
    override val hidesQuestTypes = setOf(parentQuest::class.simpleName!!, AddPathSurface::class.simpleName!!)

    override fun getStyledElements(mapData: MapDataWithGeometry): Sequence<Pair<Element, Style>> {
        val handledSurfaces = Surface.values().map { it.osmValue }.toSet() + Surface.surfaceReplacements.keys
        return mapData
           .filter( """ways, relations with
               surface or highway ~ ${(ALL_ROADS + ALL_PATHS).joinToString("|")}
               and !surface:note
               and (!surface or surface ~ ${handledSurfaces.joinToString("|") })
               """)
           .map { it to getStyle(it) }
    }

    override fun createForm(element: Element) = SurfaceOverlayForm()
}

private fun getStyle(element: Element): Style {
    val surface = createSurfaceStatus(element.tags)
    // not set but indoor or private -> do not highlight as missing
    val isNotSetButThatsOkay = (surface == null || surface in listOf(PAVED_ROAD, PAVED_AREA, UNPAVED_ROAD, UNPAVED_AREA)) && (isIndoor(element.tags) || isPrivateOnFoot(element))
    val color = if (isNotSetButThatsOkay) Color.INVISIBLE else surface.color
    val label = if("surface" in element.tags) {
        element.tags["surface"]
    } else {
        null
    }
    return if (element.tags["area"] == "yes") PolygonStyle(color, label) else PolylineStyle(color, null, null, label)
}

private val Surface?.color get() = when (this) {
    // design ideas:
    // - use not only color, somehow?
    // https://icolorpalette.com/collection/color-palette-collection
    //
    // unknown value is string pink as usual
    // no value should conflict with it
    //
    // surface=paved / surface=unpaved treated as unknown
    // TODO what about footway:surface=paving_stones cycleway:surface=asphalt surface=paved

    // in general colours should be, in order
    // - distinctive from each other
    // - not confusing, especially with other elemnets (barriers, waterways)
    // - similar values should have colours closer to each other
    //   surface=asphalt and surface=concrete should be more similar to each other
    //   than to the surface=woodchips
    // - intuitive
    // - pretty

    // special colours for cases where matching between symbolic colour
    // and actual surface is strong, such as yellow for sand

    // gray-black scale for well paved ones
    // light gray for asphalt as light value not dominating it
    // darker and stronger and more colourful, especially red
    // for surface of worse quality


    // light purple remain unused
    // https://icolorpalette.com/31-purple-color-combinations
    // https://icolorpalette.com/50-autumn-fall-color-palettes

    // declare test sites in emulator!

    /*
    for testing:
[out:json][bbox:{{bbox}}][timeout:800];

(
  way[surface=wood];
)->.wood;

(
  way[surface=woodchips];
)->.woodchips;

(
  way.woodchips(around.wood:30);
)->.woodchips_near_wood;
(
  way.wood(around.woodchips_near_wood:30);
)->.and_its_wood;

(
.woodchips_near_wood;
.and_its_wood;
);

out geom meta;
     */
    // gray works nicely for styling but looks ugly and depressing in some urban areas
    // but frankly, it is a depressing urban area in section without SC quests and this
    // is quite fitting
    //
    // but maybe asphalt/paving should get some other palette than grayscale?
    // 10b4f2 looks nicely (right now assigned to compacted)
    ASPHALT -> "#dddddd"
    PAVING_STONES -> "#999999"

    CONCRETE -> "#b0b0e0"
    CONCRETE_PLATES -> "#79a99c" // FOR TRYING: #70b090 - too green (but maybe in heavy soon glare would work well) 79a99c
    CONCRETE_LANES -> "#beadbe" // aa77aa - very purple (but maybe in heavy soon glare would work well) - try c5bfc5, b79bb7?
    SETT -> "#8888bb"
    METAL -> "#000000" // ??? really rare, whatever, it gets black

    // paved but badly
    UNHEWN_COBBLESTONE -> "#7777af"
    GRASS_PAVER -> "#aa7777"

    // special unique colors strongly matching
    GRASS -> "#70cc00" // green
    ARTIFICIAL_TURF -> "#b0ef15" // toxic green
    SAND -> "#ffff00" // yellow bfa080
    WOOD -> "#804000" // brown
    WOODCHIPS -> "#bfa080" // brown / brownish
    ROCK -> "#ccccdd" // grayish - very similar to asphalt but this surfaces are quite unlikely to be close to each other

    // cyan and related colours for various unpaved surfaces
    COMPACTED -> "#10b4f2"
    FINE_GRAVEL -> "#95d4e1"
    GRAVEL -> "#8db7c1"
    PEBBLES -> "#68869a"

    // orange
    DIRT -> "#f59709"

    CLAY -> "#ccff00" // TODO
    TARTAN -> "#ccff00" // TODO
    GROUND_ROAD, GROUND_AREA -> "#ccff00" // greenish
    PAVED_ROAD, PAVED_AREA, UNPAVED_ROAD, UNPAVED_AREA, null -> Color.UNSPECIFIED
}

private fun isIndoor(tags: Map<String, String>): Boolean = tags["indoor"] == "yes"
