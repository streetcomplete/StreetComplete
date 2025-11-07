package de.westnordost.streetcomplete.quests.artwork

import de.westnordost.streetcomplete.osm.Tags

enum class ArtworkType(val osmValue: String, val osmArtworkTypeValue: String? = null) {
    SCULPTURE("sculpture"),
    STATUE("statue"),
    MURAL("mural"),
    GRAFFITI("graffiti"),
    BUST("bust"),
    INSTALLATION("installation"),
    MOSAIC("mosaic"),
    STONE("stone"),
    PAINTING("painting"),
    RELIEF("relief"),
    ARCHITECTURE("architecture"),
    FOUNTAIN("fountain"),
    LAND_ART("land_art"),
}

fun ArtworkType.applyTo(tags: Tags) {
    tags["artwork_type"] = this.osmValue
    if (this.osmArtworkTypeValue != null) {
        tags["artwork_type"] = this.osmArtworkTypeValue
    }
}
