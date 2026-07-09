package de.westnordost.streetcomplete.quests.max_height

import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.util.math.intersects

// Returns ways that intersect with the given element, that triggered the quest.
fun highlightIntersectingStructures(
    element: Element,
    mapData: MapDataWithGeometry
): Sequence<Element> {
    if (element.type != ElementType.WAY) return emptySequence()
    val geometry = mapData.getWayGeometry(element.id) as? ElementPolylinesGeometry ?: return emptySequence()
    val ways = mapData.filter("""
    ways with (
        (
          highway ~ ${(ALL_ROADS + ALL_PATHS).joinToString("|")}
          or railway ~ rail|light_rail|subway|narrow_gauge|tram|disused|preserved|funicular|monorail
        )
        and bridge and bridge != no
      ) or (
        building = roof
        or man_made = pipeline and location = overhead
      )
      and layer
    """).toList()

    val layer = element.tags["layer"]?.toIntOrNull() ?: 0

    return ways.asSequence().filter { way ->
        val structureLayer = way.tags["layer"]?.toIntOrNull() ?: 0
        val wayGeometry = mapData.getWayGeometry(way.id)
        val intersects = wayGeometry != null && structureLayer > layer &&wayGeometry.intersects(geometry)
        intersects
    }
}
