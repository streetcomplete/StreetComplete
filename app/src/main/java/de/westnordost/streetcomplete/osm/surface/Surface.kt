package de.westnordost.streetcomplete.osm.surface

import de.westnordost.streetcomplete.osm.surface.Surface.*

data class SurfaceAndNote(val surface: Surface?, val note: String? = null)

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
    TARTAN("tartan"),

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

    // various possibly valid surfaces not supported as duplicates
    UNKNOWN(null),
}

val SELECTABLE_PITCH_SURFACES = listOf(
    GRASS, ASPHALT, SAND, CONCRETE,
    CLAY, ARTIFICIAL_TURF, TARTAN, DIRT,
    FINE_GRAVEL, PAVING_STONES, COMPACTED,
    SETT, UNHEWN_COBBLESTONE, GRASS_PAVER,
    WOOD, METAL, GRAVEL, PEBBLES,
    ROCK, PAVED, UNPAVED, GROUND
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

private val UNDERSPECIFED_SURFACES = setOf(PAVED, UNPAVED)

val Surface.shouldBeDescribed: Boolean get() = this in UNDERSPECIFED_SURFACES

val SurfaceAndNote.isComplete: Boolean get() =
    surface != null && (!surface.shouldBeDescribed || note != null)
