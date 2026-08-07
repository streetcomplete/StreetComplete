package de.westnordost.streetcomplete.quests.level

import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.util.math.contains
import de.westnordost.streetcomplete.util.math.isInMultipolygon

/** Returns a list of polygons, a subset of [mallGeometries], which contain elements with different
 *  `level` tagged. */
fun getMultiLevelMallGeometries(
    mallGeometries: List<ElementPolygonsGeometry>,
    mapData: MapDataWithGeometry
): List<ElementPolygonsGeometry> {
    // get all elements that have level tagged
    val thingsWithLevel = mapData.filter { it.tags.containsKey("level") }
    if (thingsWithLevel.isEmpty()) return emptyList()

    // with this, find malls that contain elements that have different levels tagged
    return mallGeometries.filter { mallGeometry ->
        var level: String? = null
        for (element in thingsWithLevel) {
            val pos = mapData.getGeometry(element.type, element.id)?.center ?: continue
            if (!mallGeometry.bounds.contains(pos)) continue
            if (!pos.isInMultipolygon(mallGeometry.polygons)) continue

            if (level != null) {
                if (level != element.tags["level"]) return@filter true
            } else {
                level = element.tags["level"]
            }
        }
        return@filter false
    }
}
