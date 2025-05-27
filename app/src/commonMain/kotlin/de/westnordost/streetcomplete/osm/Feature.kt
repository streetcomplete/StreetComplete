package de.westnordost.streetcomplete.osm

import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Relation
import de.westnordost.streetcomplete.data.osm.mapdata.Way

/** Apply this feature to the given [tags], optionally removing a [previousFeature] first, i.e.
 *  replacing it. */
fun Feature.applyTo(tags: Tags, previousFeature: Feature? = null) {
    if (previousFeature != null) {
        for ((key, value) in previousFeature.removeTags) {
            if (tags[key] == value) tags.remove(key)
        }
        for (key in previousFeature.removeTagKeys) {
            tags.remove(key)
        }
    }
    for ((key, value) in addTagKeys.associateWith { "yes" } + addTags) {
        if (key !in tags || preserveTags.none { it.containsMatchIn(key) }) {
            tags[key] = value
        }
    }
}

/** Return an exemplary element that would match this feature. */
fun Feature.toElement(): Element {
    val allTags = tagKeys.associateWith { "yes" } + tags
    return when {
        GeometryType.POINT in geometry ||
        GeometryType.VERTEX in geometry -> {
            Node(-1L, NULL_ISLAND, allTags)
        }
        GeometryType.LINE in geometry || GeometryType.AREA in geometry -> {
            Way(-1L, NULL_ISLAND_NODES, allTags)
        }
        GeometryType.RELATION in geometry -> {
            Relation(-1L, listOf(), allTags)
        }
        else -> {
            Node(-1L, NULL_ISLAND, allTags)
        }
    }
}

private val NULL_ISLAND = LatLon(0.0, 0.0)
private val NULL_ISLAND_NODES = listOf(-1L, -2L, -3L, -1L)
