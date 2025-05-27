package de.westnordost.streetcomplete.osm.surface

enum class Surface(val osmValue: String?) {
    // paved
    ASPHALT("asphalt"),
    CONCRETE("concrete"),
    CONCRETE_LANES("concrete:lanes"),
    PAVING_STONES("paving_stones"),
    SETT("sett"),
    UNHEWN_COBBLESTONE("unhewn_cobblestone"),
    GRASS_PAVER("grass_paver"),
    METAL("metal"),
    WOOD("wood"),

    // unpaved
    COMPACTED("compacted"),
    WOODCHIPS("woodchips"),
    FINE_GRAVEL("fine_gravel"),
    PEBBLES("pebblestone"),
    GRAVEL("gravel"),

    // natural
    DIRT("dirt"),
    MUD("mud"),
    GRASS("grass"),
    SAND("sand"),
    ROCK("rock"),

    // sports
    CLAY("clay"),
    ARTIFICIAL_TURF("artificial_turf"),
    RUBBER("rubber"),
    ACRYLIC("acrylic"),

    // generic surfaces
    PAVED("paved"),
    UNPAVED("unpaved"),
    GROUND("ground"),

    UNSUPPORTED(null);

    companion object {
        /** Selectable surface values for roads, paths, etc. */
        val selectableValuesForWays: List<Surface> = listOf(
            // paved surfaces
            ASPHALT, PAVING_STONES, CONCRETE, CONCRETE_LANES,
            SETT, UNHEWN_COBBLESTONE, GRASS_PAVER, WOOD, METAL,
            // unpaved surfaces
            COMPACTED, FINE_GRAVEL, GRAVEL, PEBBLES, WOODCHIPS,
            // ground surfaces
            DIRT, MUD, GRASS, SAND, ROCK,
            // generic surfaces
            PAVED, UNPAVED, GROUND
        )

        /** Selectable surface values for sport pitches */
        val selectableValuesForPitches: List<Surface> = listOf(
            // grouped a bit: 1. very most popular, 2. artificial, 3. natural
            GRASS, ASPHALT, CONCRETE,
            ARTIFICIAL_TURF, ACRYLIC, RUBBER,
            CLAY, SAND, DIRT,
            // then, roughly by popularity
            FINE_GRAVEL, PAVING_STONES, COMPACTED,
            SETT, UNHEWN_COBBLESTONE, GRASS_PAVER,
            WOOD, METAL, GRAVEL,
            PEBBLES, ROCK,
            PAVED, UNPAVED, GROUND
        )

        /** A map of tag value to Surface type that should be treated as aliases of known Surface
         *  types, i.e. that are displayed as that Surface but whose tag is not modified when saving
         *  it again. */
        val aliases: Map<String, Surface> = mapOf(
            // sorted roughly by usage count

            "concrete:plates" to CONCRETE, // very specific subtype of concrete. See #6265 why it is
                                           // not selectable anymore. (Too easy to tag "wrongly")
            "earth" to DIRT, // 10x lesser used synonym of dirt
            "soil" to DIRT, // least-used synonym of dirt, not mentioned on Key:surface page

            "tartan" to RUBBER, // a brand name for bound rubber granules

            "bricks" to PAVING_STONES, // subtype of paving stones, documented
            "brick" to PAVING_STONES, // ...same, both tags fight for dominance, not documented

            "chipseal" to ASPHALT, // subtype/asphalt-alike surface

            "metal_grid" to METAL, // more specific than metal
        )
    }
}
