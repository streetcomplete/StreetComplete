package de.westnordost.streetcomplete.quests.artwork

import de.westnordost.streetcomplete.osm.Tags

enum class ArtworkType(val osmValue: String, val osmArtworkTypeValue: String? = null) {
    SCULPTURE("sculpture"),
    STATUE("statue"),
    BUST("bust"),
    ARCHITECTURE("architecture"),
    RELIEF("relief"),
    MURAL("mural"),
    FOUNTAIN("fountain"),
    INSTALLATION("installation"),
    STONE("stone"),
    MOSAIC("mosaic"),
    GRAFFITI("graffiti"),
    PAINTING("painting"),
    LAND_ART("land_art"),
}

fun ArtworkType.applyTo(tags: Tags) {
    tags["artwork_type"] = this.osmValue
    if (this.osmArtworkTypeValue != null) {
        tags["artwork_type"] = this.osmArtworkTypeValue
    }
}
