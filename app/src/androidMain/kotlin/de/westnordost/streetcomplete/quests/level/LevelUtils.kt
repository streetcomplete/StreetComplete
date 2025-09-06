package de.westnordost.streetcomplete.quests.level

import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.util.math.contains
import de.westnordost.streetcomplete.util.math.isInMultipolygon

val thingsWithLevelFilter by lazy { """
        nodes, ways, relations with level
    """.toElementFilterExpression() }

/* only nodes because ways/relations are not likely to be floating around freely in a mall
 * outline */
val filter by lazy { """
        nodes with
          !level
          and (name or brand or noname = yes or name:signed = no)
    """.toElementFilterExpression() }

fun getMultiLevelMallGeometries(
    mallGeometries: List<ElementPolygonsGeometry>,
    thingsWithLevel: Iterable<Element>,
    mapData: MapDataWithGeometry
): List<ElementPolygonsGeometry> {
    // with this, find malls that contain elements that have different levels tagged
    return mallGeometries.filter { mallGeometry ->
        var level: String? = null
        for (element in thingsWithLevel) {
            val pos = mapData.getGeometry(element.type, element.id)?.center ?: continue
            if (!mallGeometry.bounds.contains(pos)) continue
            if (!pos.isInMultipolygon(mallGeometry.polygons)) continue

            if (element.tags.containsKey("level")) {
                if (level != null) {
                    if (level != element.tags["level"]) return@filter true
                } else {
                    level = element.tags["level"]
                }
            }
        }
        return@filter false
    }
}
