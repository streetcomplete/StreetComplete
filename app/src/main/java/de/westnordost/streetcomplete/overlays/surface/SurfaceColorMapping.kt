package de.westnordost.streetcomplete.overlays.surface

import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.Surface.*
import de.westnordost.streetcomplete.overlays.Color

val Surface?.color get() = when (this) {
    // https://davidmathlogic.com/colorblind/#%23444444-%23FF0000-%231A87E6-%232FACE8-%2330D4EE-%2310C1B8-%230DA082-%23F37D1E-%23EEBD0D-%23B6EF28
    ASPHALT, CONCRETE, PAVING_STONES, WOOD, METAL -> Color.BLUE
    CONCRETE_PLATES, CONCRETE_LANES, SETT -> Color.SKY
    COMPACTED -> Color.CYAN
    UNHEWN_COBBLESTONE -> Color.AQUAMARINE
    GRASS_PAVER -> Color.TEAL
    SAND  -> Color.ORANGE
    GRASS  -> Color.LIME
    DIRT, GROUND_ROAD, GROUND_AREA, WOODCHIPS -> Color.GOLD
    FINE_GRAVEL -> "#dddddd"
    GRAVEL, PEBBLES, ROCK -> "#999999"
    CLAY, ARTIFICIAL_TURF, TARTAN -> Color.BLACK // not encountered in normal situations, get the same as surface with surface:note
    PAVED_ROAD, PAVED_AREA, UNPAVED_ROAD, UNPAVED_AREA, null -> Color.DATA_REQUESTED
}    /*
    // TODO create graphs as illustration of design process
    categorizing surface into 8+2 groups

    missing surface (including paved/unpaved) vs missing surface (including paved/unpaved) with note vs surface

    fundamental split:
    all: paved vs unpaved
    paved: flat and flattish vs unhewn_cobblestone
    flat and flattish paved: asphalt/concrete/paving_stones/wood/metal vs sett/concrete:plates vs concrete:lanes
    unpaved: extremely bad for cyclists (sand, grass paver), high quality (compacted), all other
    extremely bad for cyclists: sand, grass paver
    unpaved_other: dirt/gravel/pebblestone/rock/ground/woodchips/fine_gravel vs grass

    special, not really on roads: clay, artificial_turf, tartan



    TODO - try using colours from the offical palette
    TODO consider unifying paving stones and asphalt
    // blue
    const val BLUE = "#1A87E6"
    const val SKY = "#2FACE8"
    const val CYAN = "#30D4EE"
    // green-ish
    const val AQUAMARINE = "#10C1B8"
    const val TEAL = "#0DA082"
    // orange-yellow
    const val ORANGE = "#F37D1E"
    const val GOLD = "#EEBD0D"
    const val LIME = "#B6EF28"
     */

    // current one:
    // https://davidmathlogic.com/colorblind/#%23DDDDDD-%23999999-%23B0B0E0-%2379A99C-%23BEADBE-%238888BB-%23000000-%237777AF-%23AA7777-%2370CC00-%23B0EF15-%23FFFF00-%23804000-%23BFA080-%23CCCCDD-%2310B4F2-%2395D4E1-%238DB7C1-%2368869A-%23F59709-%23CCFF00-%23CCFF00-%23CCFF00-%23FF0000

    // design ideas:
    // - use not only color, somehow?
    // https://icolorpalette.com/collection/color-palette-collection
    //
    // unknown value is crimson Color.DATA_REQUESTED as usual
    // no value should conflict with it
    //
    // surface=paved / surface=unpaved treated as unknown
    // in case like
    // footway:surface=paving_stones cycleway:surface=asphalt surface=paved
    // overlay asking for colour needs to decide which surface should be used

    // in general colours should be, in order
    // - distinctive from each other
    // - not confusing, especially with other elements (barriers, waterways)
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

    /*
    // attempt that tried to assign intuitive colours (gray asphalt, brown wood, yellow sand) and ignore colour blindness
    Surface.ASPHALT -> "#dddddd"
    Surface.PAVING_STONES -> "#999999"

    Surface.CONCRETE -> "#b0b0e0"
    Surface.CONCRETE_PLATES -> "#79a99c" // FOR TRYING: #70b090 - too green (but maybe in heavy soon glare would work well) 79a99c
    Surface.CONCRETE_LANES -> "#beadbe" // aa77aa - very purple (but maybe in heavy soon glare would work well) - try c5bfc5, b79bb7?
    Surface.SETT -> "#8888bb"
    Surface.METAL -> "#000000" // ??? really rare, whatever, it gets black

    // paved but badly
    Surface.UNHEWN_COBBLESTONE -> "#7777af"
    Surface.GRASS_PAVER -> "#aa7777"

    // special unique colors strongly matching
    Surface.GRASS -> "#70cc00" // green
    Surface.ARTIFICIAL_TURF -> "#b0ef15" // toxic green
    Surface.SAND -> "#ffff00" // yellow bfa080
    Surface.WOOD -> "#804000" // brown
    Surface.WOODCHIPS -> "#bfa080" // brown / brownish
    Surface.ROCK -> "#ccccdd" // grayish - very similar to asphalt but this surfaces are quite unlikely to be close to each other

    // cyan and related colours for various unpaved surfaces
    Surface.COMPACTED -> "#10b4f2"
    Surface.FINE_GRAVEL -> "#95d4e1"
    Surface.GRAVEL -> "#8db7c1"
    Surface.PEBBLES -> "#68869a"

    // orange
    Surface.DIRT -> "#f59709"

    Surface.CLAY -> "#ccff00" // TODO
    Surface.TARTAN -> "#ccff00" // TODO
    Surface.GROUND_ROAD, Surface.GROUND_AREA -> "#ccff00" // greenish
    Surface.PAVED_ROAD, Surface.PAVED_AREA, Surface.UNPAVED_ROAD, Surface.UNPAVED_AREA, null -> Color.DATA_REQUESTED

     */

