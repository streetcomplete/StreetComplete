package de.westnordost.streetcomplete.quests.roof_orientation

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BUILDING
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.util.math.flatDistanceTo
import de.westnordost.streetcomplete.util.math.flatDistanceToArc
import de.westnordost.streetcomplete.util.math.measuredLength
import kotlin.math.abs
import kotlin.math.max

class AddRoofOrientation : OsmElementQuestType<String> {

    private val roofsFilter by lazy { """
        ways with
          roof:shape = gabled
          and !roof:orientation
          and !roof:direction
          and building
          and building !~ no|construction
          and location != underground
          and ruins != yes
    """.toElementFilterExpression() }

    override val changesetComment = "Add roof orientation"
    override val wikiLink = "Key:roof:orientation"
    override val icon = R.drawable.ic_quest_roof_orientation
    override val achievements = listOf(BUILDING)
    override val defaultDisabledMessage = R.string.default_disabled_msg_roof

    override fun getTitle(tags: Map<String, String>) = R.string.quest_roofOrientation_title

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.ways.filter { way ->
            if (!way.isClosed || way.nodeIds.size !in 5..20 || !roofsFilter.matches(way)) {
                return@filter false
            }

            val nodeIds = way.nodeIds.dropLast(1) // last equals first for closed ways
            val points = nodeIds.mapNotNull { mapData.getNode(it)?.position }
            isRectangularOutline(points)
        }

    override fun isApplicableTo(element: Element) =
        if (roofsFilter.matches(element)) null else false

    override fun createForm() = AddRoofOrientationForm()

    override fun applyAnswerTo(
        answer: String,
        tags: Tags,
        geometry: ElementGeometry,
        timestampEdited: Long,
    ) {
        tags["roof:orientation"] = answer
    }
}

private fun isRectangularOutline(points: List<LatLon>): Boolean {
    val rectangle = findAllQuadrangles(points)
        .filter { isNearlyRectangular(it) }
        .maxByOrNull { it.circumference }

    if (rectangle == null || isNearlySquare(rectangle)) {
        return false
    }

    // Exclude rectangles that differ too much from the whole outline
    if (rectangle.circumference < points.circumference() * 0.75) {
        return false
    }

    // Check that all other points lie near the rectangle's sides
    val remainingPoints = points.toSet() - rectangle.toSet()
    if (remainingPoints.isEmpty()) {
        return true
    }

    val sides = rectangle.sidesWithLengths()
    return remainingPoints.all { point ->
        sides.any { (side, length) -> point.flatDistanceToArc(side) < 0.1 * length }
    }
}

/** Returns all 4-point-subsets that could form a rectangle */
private fun findAllQuadrangles(points: List<LatLon>): Sequence<Quadrangle> = sequence {
    val n = points.size
    for (i in 0 until n - 3) {
        for (j in i + 1 until n - 2) {
            for (k in j + 1 until n - 1) {
                for (l in k + 1 until n) {
                    yield(Quadrangle(points[i], points[j], points[k], points[l]))
                }
            }
        }
    }
}

private fun approximatelyEqual(length1: Double, length2: Double, tolerance: Double): Boolean =
    abs(length1 - length2) <= tolerance

/**
 * Returns true if the four corners of the quadrangle [q] form a rectangle within an allowed tolerance.
 */
private fun isNearlyRectangular(q: Quadrangle): Boolean {
    if (
        !approximatelyEqual(q.sideA, q.sideC, 0.1 * q.maxBD) ||
        !approximatelyEqual(q.sideB, q.sideD, 0.1 * q.maxAC)
    ) {
        return false
    }

    val diagonal1 = q.corner0.flatDistanceTo(q.corner2)
    val diagonal2 = q.corner1.flatDistanceTo(q.corner3)

    return approximatelyEqual(diagonal1, diagonal2, 0.1 * max(diagonal1, diagonal2))
}

/**
 * Returns true if the four corners of the quadrangle [q] form a square within an allowed tolerance.
 */
private fun isNearlySquare(q: Quadrangle): Boolean =
    approximatelyEqual(q.maxAC, q.maxBD, 0.1 * max(q.maxAC, q.maxBD))

private fun List<LatLon>.circumference() = (this + first()).measuredLength()
private fun LatLon.flatDistanceToArc(arc: Pair<LatLon, LatLon>) = flatDistanceToArc(arc.first, arc.second)

private data class Quadrangle(
    val corner0: LatLon,
    val corner1: LatLon,
    val corner2: LatLon,
    val corner3: LatLon,
) {
    val sideA = corner0.flatDistanceTo(corner1)
    val sideB = corner1.flatDistanceTo(corner2)
    val sideC = corner2.flatDistanceTo(corner3)
    val sideD = corner3.flatDistanceTo(corner0)

    val maxAC = max(sideA, sideC)
    val maxBD = max(sideB, sideD)

    val circumference = sideA + sideB + sideC + sideD
}

private fun Quadrangle.toSet() = setOf(corner0, corner1, corner2, corner3)
private fun Quadrangle.sidesWithLengths() = setOf(
    Pair(corner0 to corner1, sideA),
    Pair(corner1 to corner2, sideB),
    Pair(corner2 to corner3, sideC),
    Pair(corner3 to corner0, sideD),
)
