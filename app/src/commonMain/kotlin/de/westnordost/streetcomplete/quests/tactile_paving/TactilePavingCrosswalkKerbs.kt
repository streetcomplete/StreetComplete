package de.westnordost.streetcomplete.quests.tactile_paving

import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.MapData
import de.westnordost.streetcomplete.util.ktx.firstAndLast

/* The crossing quest covers both end kerbs. Individual kerb quests are only needed when the
 * crossing is tagged no, partial, or incorrect. */

/** Kerb nodes whose tactile paving situation is unknown or stale. */
internal val kerbsWithUnknownTactilePavingFilter by lazy { """
    nodes with
      !tactile_paving
      or tactile_paving = unknown
      or tactile_paving = no and tactile_paving older today -8 years
      or tactile_paving = yes and tactile_paving older today -12 years
""".toElementFilterExpression() }

/** Nodes representing pedestrian crossings. */
internal val tactilePavingCrossingsFilter by lazy { """
    nodes with
      highway = traffic_signals and crossing = traffic_signals and foot != no
      or highway = crossing and foot != no
""".toElementFilterExpression() }

/** Crossing nodes that cover the tactile-paving question for their end kerbs. */
private val tactilePavingCrossingsCoveringKerbsFilter by lazy { """
    nodes with
      (
        highway = traffic_signals and crossing = traffic_signals and foot != no
        or highway = crossing and foot != no
      )
      and (
        !tactile_paving
        or tactile_paving !~ no|partial|incorrect
        or tactile_paving older today -8 years
      )
""".toElementFilterExpression() }

/** Ways on whose crossing nodes the crosswalk quest is not asked. */
internal val tactilePavingCrosswalkExcludedWaysFilter by lazy { """
    ways with
      highway = cycleway and foot !~ yes|designated
      or highway = service and service = driveway
      or highway and access ~ private|no
""".toElementFilterExpression() }

/** Returns crosswalk end nodes covered by a crossing-node tactile-paving question. */
internal fun MapData.findCrosswalkEndNodeIdsCoveredByCrossingNode(): Set<Long> {
    val excludedWayNodeIds = ways
        .filter { tactilePavingCrosswalkExcludedWaysFilter.matches(it) }
        .flatMapTo(HashSet()) { it.nodeIds }

    val crossingNodeIds = nodes
        .filter { tactilePavingCrossingsCoveringKerbsFilter.matches(it) && it.id !in excludedWayNodeIds }
        .mapTo(HashSet()) { it.id }
    if (crossingNodeIds.isEmpty()) return emptySet()

    return ways.asSequence()
        .filter { way -> way.tags["footway"] == "crossing" && way.nodeIds.any { it in crossingNodeIds } }
        .flatMapTo(HashSet()) { it.nodeIds.firstAndLast() }
}
