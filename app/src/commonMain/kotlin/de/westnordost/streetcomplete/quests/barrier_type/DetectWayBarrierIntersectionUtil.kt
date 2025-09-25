package de.westnordost.streetcomplete.quests.barrier_type

import de.westnordost.streetcomplete.data.elementfilter.ElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.osm.findNodesAtCrossingsOf

fun detectWayBarrierIntersection(mapData: MapDataWithGeometry, barrierFilter: ElementFilterExpression, pathsFilter: ElementFilterExpression): Iterable<Element> {
    val barrierWays = mapData.ways.asSequence()
        .filter { barrierFilter.matches(it) }

    val movingWays = mapData.ways.asSequence()
        .filter { pathsFilter.matches(it) }

    /* skip all cases involving tunnels, as it is typical to map retaining wall as
     * intersecting path, what is even kind of correct
     *
     * e.g. https://www.openstreetmap.org/node/5074693713
     *
     * For bridges it is more dubious but also in active use, see
     * https://www.openstreetmap.org/node/78135912 + https://www.openstreetmap.org/way/417857886
     * https://www.openstreetmap.org/node/308681095 + https://www.openstreetmap.org/way/558083525
     */
    val crossings = findNodesAtCrossingsOf(barrierWays, movingWays, mapData).filter {
        it.movingWays.all { way ->
            (!way.tags.containsKey("tunnel") || way.tags["tunnel"] == "no") &&
            (!way.tags.containsKey("bridge") || way.tags["bridge"] == "no")
        }
    }
    return crossings.map { it.node }.filter { it.tags.isEmpty() }
}
