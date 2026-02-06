package de.westnordost.streetcomplete.data.osm.geometry.polygons

import PriorityQueue
import kotlin.math.min
import kotlin.math.sqrt

/*
    Implementation of the polylabel algorithm inspired by mapbox's implementation in java.
    See here : https://github.com/mapbox/polylabel
 */
object PolygonAlgorithms {

    /* Simple centroid algorithm */
    fun centroid(polygon: Polygon): Point {
        val points = polygon.shape
        var sumX = 0.0
        var sumY = 0.0
        for (p in points) {
            sumX += p.x
            sumY += p.y
        }
        return Point(sumX / points.size, sumY / points.size)
    }

    /* Core of the problem : visual center (within the polygon) */
    /* Set up of the variables */
    fun polylabel(polygon: Polygon, precision: Double = 1.0): Point {
        val minX = polygon.shape.minOf { it.x }
        val maxX = polygon.shape.maxOf { it.x }
        val minY = polygon.shape.minOf { it.y }
        val maxY = polygon.shape.maxOf { it.y }

        val width = maxX - minX
        val height = maxY - minY
        val cellSize = (min(width, height) / 10.0)
        val halfCell = cellSize / 2.0

        val queue = PriorityQueue<Cell>()

        // Initialize grid, skip cells inside holes
        var x = minX
        while (x < maxX) {
            var y = minY
            while (y < maxY) {
                val center = Point(x + halfCell, y + halfCell)
                if (isPointInRing(center, polygon.shape) &&
                    polygon.holes.none { isPointInRing(center, it) }) {
                    val distance = pointToPolygonDist(center, polygon)
                    queue.add(Cell(center.x, center.y, halfCell, distance))
                }
                y += cellSize
            }
            x += cellSize
        }

        var best: Cell? = null

        while (queue.isNotEmpty()) {
            val cell = queue.poll()

            if (best == null || cell.distance > best.distance) best = cell

            if (cell.max - (best?.distance ?: 0.0) <= precision) continue

            val h = cell.half / 2.0
            val children = listOf(
                Point(cell.centerX - h, cell.centerY - h),
                Point(cell.centerX + h, cell.centerY - h),
                Point(cell.centerX - h, cell.centerY + h),
                Point(cell.centerX + h, cell.centerY + h)
            )

            for (c in children) {
                if (isPointInRing(c, polygon.shape) &&
                    polygon.holes.none { isPointInRing(c, it) }) {
                    queue.add(Cell(c.x, c.y, h, pointToPolygonDist(c, polygon)))
                }
            }
        }

        return Point(best!!.centerX, best.centerY)
    }

    fun pointToPolygonDist(point: Point, polygon: Polygon): Double {
        // Is the point inside the outer shape?
        val insideOuter = isPointInRing(point, polygon.shape)

        // Distance to outer shape
        var minDistSq = ringDistanceSq(point, polygon.shape)

        // Distance to holes
        for (hole in polygon.holes) {
            val insideHole = isPointInRing(point, hole)
            val distSqHole = ringDistanceSq(point, hole)

            if (insideHole) {
                // Inside a hole → consider outside polygon
                return -sqrt(distSqHole)
            }

            // Outside hole, may be closer than outer
            minDistSq = minOf(minDistSq, distSqHole)
        }

        return if (insideOuter) sqrt(minDistSq) else -sqrt(minDistSq)
    }

    private fun pointToSegmentDistSq(pointToObserve: Point, pointA: Point, pointB: Point): Double {
        var x = pointA.x
        var y = pointA.y
        var dx = pointB.x - x
        var dy = pointB.y - y

        if (dx != 0.0 || dy != 0.0) {
            val t = ((pointToObserve.x - x) * dx + (pointToObserve.y - y) * dy) / (dx * dx + dy * dy)
            when {
                t > 1 -> { x = pointB.x; y = pointB.y }
                t > 0 -> { x += dx * t; y += dy * t }
            }
        }

        dx = pointToObserve.x - x
        dy = pointToObserve.y - y
        return dx * dx + dy * dy
    }

    private fun isPointInRing(point: Point, ring: List<Point>): Boolean {
        var inside = false
        for (i in ring.indices) {
            val pointA = ring[i]
            val pointB = ring[(i + 1) % ring.size]

            val intersects = ((pointA.y > point.y) != (pointB.y > point.y)) &&
                (point.x < (pointB.x - pointA.x) * (point.y - pointA.y) / (pointB.y - pointA.y) + pointA.x)

            if (intersects) inside = !inside
        }
        return inside
    }

    private fun ringDistanceSq(point: Point, ring: List<Point>): Double {
        var minDistSq = Double.POSITIVE_INFINITY
        for (i in ring.indices) {
            val pointA = ring[i]
            val pointB = ring[(i + 1) % ring.size]
            val distSq = pointToSegmentDistSq(point, pointA, pointB)
            if (distSq < minDistSq) minDistSq = distSq
        }
        return minDistSq
    }
}
