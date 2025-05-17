package de.westnordost.streetcomplete.osm.surface

import de.westnordost.streetcomplete.osm.surface.Surface.*

enum class Surface(val osmValue: String?) {
    ASPHALT("asphalt"),
    CONCRETE("concrete"),
    CONCRETE_PLATES("concrete:plates"),
    CONCRETE_LANES("concrete:lanes"),
    FINE_GRAVEL("fine_gravel"),
    PAVING_STONES("paving_stones"),
    COMPACTED("compacted"),
    DIRT("dirt"),
    MUD("mud"),
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
    RUBBER("rubber"),
    ACRYLIC("acrylic"),

    // generic surfaces
    PAVED("paved"),
    UNPAVED("unpaved"),
    GROUND("ground"),

    // extra values, handled as synonyms (not selectable)
    EARTH("earth"), // synonym of "dirt"
    CHIPSEAL("chipseal"), // subtype/synonym of asphalt
    METAL_GRID("metal_grid"), // more specific than "metal"

    // these values ideally would be removed from OpenStreetMap, but while they remain
    // we want to handle them as synonyms
    SOIL("soil"), // synonym of earth and dirt
    PAVING_STONES_WITH_WEIRD_SUFFIX("paving_stones:30"), // https://wiki.openstreetmap.org/wiki/Tag%3Asurface%3Dpaving_stones%3A30
    COBBLESTONE_FLATTENED("cobblestone:flattened"), // =sett with good smoothness
    BRICK("brick"),
    BRICKS("bricks"),
    TARTAN("tartan"), // there are two products by 3M named "Tartan":
                      // "Tartan track" are bound rubber granules, "Tartan turf" is artificial turf.
                      // Very likely we mean "bound rubber granules", but still, it is inherently ambiguous
    HARD("hard"), // badly worded: surface used for tennis hard courts, which is synthetic resin (-> acrylic)

    // various possibly valid surfaces not supported as duplicates
    UNKNOWN(null),
}

val SELECTABLE_PITCH_SURFACES = listOf(
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

val SELECTABLE_WAY_SURFACES = listOf(
    // paved surfaces
    ASPHALT, PAVING_STONES, CONCRETE, CONCRETE_PLATES, CONCRETE_LANES,
    SETT, UNHEWN_COBBLESTONE, GRASS_PAVER, WOOD, METAL,
    // unpaved surfaces
    COMPACTED, FINE_GRAVEL, GRAVEL, PEBBLES, WOODCHIPS,
    // ground surfaces
    DIRT, MUD, GRASS, SAND, ROCK,
    // generic surfaces
    PAVED, UNPAVED, GROUND
)
