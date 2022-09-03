package de.westnordost.streetcomplete.overlays.surface

import de.westnordost.streetcomplete.osm.Surface
import de.westnordost.streetcomplete.overlays.Color

val Surface?.color get() = when (this) {
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
    Surface.PAVED_ROAD, Surface.PAVED_AREA, Surface.UNPAVED_ROAD, Surface.UNPAVED_AREA, null -> Color.UNSPECIFIED
}
