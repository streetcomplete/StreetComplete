package de.westnordost.streetcomplete.osm.kerb

import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.MapData
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.LAST_CHECK_DATE_KEYS
import de.westnordost.streetcomplete.osm.getLastCheckDateKeys
import de.westnordost.streetcomplete.util.ktx.allExceptFirstAndLast
import de.westnordost.streetcomplete.util.ktx.firstAndLast

private val footwaysFilter by lazy { """
    ways with (
        highway ~ footway|path
        or highway = cycleway and foot ~ yes|designated
      )
      and area != yes
      and access !~ private|no and foot !~ no
""".toElementFilterExpression() }

private val waysFilter by lazy { """
    ways with
      highway ~ ${(ALL_ROADS + ALL_PATHS).joinToString("|")}
      or construction ~ ${(ALL_ROADS + ALL_PATHS).joinToString("|")}
""".toElementFilterExpression() }

/* It is documented to be legal for a barrier=kerb to be mapped on the highway=crossing. See also
 * https://taginfo.openstreetmap.org/keys/kerb#combinations . At the time of writing, 40% of
 * all kerbs are mapped this way.
 *
 * This app should however not ask for details on these because the question is then confusing: It
 * asks for "this kerb" but points to the crossing, i.e. the middle of the street.
 *
 * So, if the candidate has any tags that are NOT one of these, the app does not recognize this
 * node as a kerb, even if barrier=kerb is set */
private val allowedKeysOnKerbNode = setOf(
    "barrier", // = kerb
    // details on kerbs
    "sloped_curb",
    "tactile_paving",
    "surface", "smoothness", "material",
    "kerb", "kerb:height", "height", "width",
    // access/eligibility-related
    "wheelchair", "bicycle", "foot", "stroller",
    // misc / meta info
    "source", "project", "note", "mapillary"
    // check date
) + LAST_CHECK_DATE_KEYS

/* separating regex-enabled keys and normal keys for performance reasons. No need to regex on
   strings that are not regexes ... */
private val allowedKeysOnKerbNodeRegexes = getLastCheckDateKeys(".*").map { it.toRegex() }

/** Most nodes **could** be a kerb, depending on their location within a way. However, nodes that
 *  are already something else, e.g. shop=hairdresser are definitely NOT a kerb.
 *
 *  If any node **could** be a kerb, this would lead to an unacceptable performance hit when any
 *  node is updated due to an answered quest. See
 *  https://github.com/streetcomplete/StreetComplete/pull/3104#issuecomment-889833381 for more
 *  details on why this function exists */
fun Node.couldBeAKerb(): Boolean = tags.keys.all { key ->
    key in allowedKeysOnKerbNode || allowedKeysOnKerbNodeRegexes.any { regex -> regex.matches(key) }
}

/** Find all nodes that are (very likely) kerbs in this map data:
 *  1. nodes tagged with barrier=kerb
 *  2. the shared nodes at intersections of barrier=kerb ways with footways
 *  3. certain shared nodes between a footway=sidewalk and a footway=crossing */
fun MapData.findAllKerbNodes(): Iterable<Node> {
    val footwayNodes = mutableSetOf<Node>()
    ways.asSequence()
        .filter { footwaysFilter.matches(it) }
        .flatMap { it.nodeIds }
        .mapNotNullTo(footwayNodes) { nodeId ->
            getNode(nodeId)?.takeIf { it.couldBeAKerb() }
        }

    val kerbBarrierNodeIds = mutableSetOf<Long>()
    ways.asSequence()
        .filter { it.tags["barrier"] == "kerb" }
        .flatMapTo(kerbBarrierNodeIds) { it.nodeIds }

    val anyWays = ways.filter { waysFilter.matches(it) }
    val crossingEndNodeIds = findCrossingKerbEndNodeIds(anyWays)

    // Kerbs can be defined in three ways (see https://github.com/streetcomplete/StreetComplete/issues/1305#issuecomment-688333976):
    return footwayNodes.filter {
        // 1. either as a node tagged with barrier = kerb on a footway
        it.tags["barrier"] == "kerb"
        // 2. or as the shared node at which a way tagged with barrier = kerb crosses a footway
        || it.id in kerbBarrierNodeIds
        // 3. or implicitly as the shared node between a footway tagged with footway = crossing and
        //    another tagged with footway = sidewalk that is the continuation of the way and is not
        //    and intersection (thus, has exactly two connections: to the sidewalk and to the crossing)
        || it.id in crossingEndNodeIds
    }
}

/** Find all node ids of end nodes of crossings that are (very probably) kerbs within the given
 *  collection of [ways] */
private fun findCrossingKerbEndNodeIds(ways: Collection<Way>): Set<Long> {
    /* using asSequence in this function so to not copy potentially huge amounts (e.g. almost all
       nodes of all ways in the data set) of data into temporary lists */

    val footways = ways.filter { footwaysFilter.matches(it) }

    val crossingEndNodeIds = footways.asSequence()
        .filter { it.tags["footway"] == "crossing" || it.tags["footway"] == "access_aisle" }
        .flatMap { it.nodeIds.firstAndLast() }

    val connectionsById = mutableMapOf<Long, Int>()
    for (id in crossingEndNodeIds) {
        val count = connectionsById[id] ?: 0
        connectionsById[id] = count + 1
    }
    // skip nodes that have not exactly ONE connection to a crossing
    connectionsById.entries.removeAll { it.value != 1 }
    if (connectionsById.isEmpty()) return emptySet()

    val sidewalkEndNodeIds = footways.asSequence()
        .filter { it.tags["footway"] == "sidewalk" || it.tags["footway"] == "traffic_island" }
        .flatMap { it.nodeIds.firstAndLast() }

    for (id in sidewalkEndNodeIds) {
        val count = connectionsById[id] ?: continue
        connectionsById[id] = count + 1
    }
    // skip nodes that have not exactly ONE connection to a sidewalk (1 to crossing + 1 to sidewalk = 2)
    connectionsById.entries.removeAll { it.value != 2 }
    if (connectionsById.isEmpty()) return emptySet()

    // skip nodes that share an end node with a way where it is not clear if it is a sidewalk, crossing or something else
    ways.asSequence()
        .filter { it.tags["footway"] !in handledFootwayValues }
        .flatMap { it.nodeIds.firstAndLast() }
        .forEach { connectionsById.remove(it) }
    if (connectionsById.isEmpty()) return emptySet()

    // skip nodes that share an end node with any node of a way that is not an end node
    ways.asSequence()
        .flatMap { it.nodeIds.allExceptFirstAndLast() }
        .forEach { connectionsById.remove(it) }
    if (connectionsById.isEmpty()) return emptySet()

    return connectionsById.keys
}

private val handledFootwayValues = setOf("sidewalk", "traffic_island", "crossing", "access_aisle")
