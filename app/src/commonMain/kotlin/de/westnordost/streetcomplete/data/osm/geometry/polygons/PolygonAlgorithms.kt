package de.westnordost.streetcomplete.data.osm.geometry.polygons

import kotlin.math.min

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
        var minX = polygon.shape.first().x
        var maxX = polygon.shape.first().x
        var minY = polygon.shape.first().y
        var maxY = polygon.shape.first().y
        for (pts in polygon.shape) {
            if (pts.x < minX) minX = pts.x
            if (pts.x > maxX) maxX = pts.x
            if (pts.y < minY) minY = pts.y
            if (pts.y > maxY) maxY = pts.y
        }
        var width = maxX - minX
        var height = maxY - minY

        val cellSize = minOf(width, height)
        val halfCellSize = cellSize / 2
        val queue = PriorityQueue<Cell>()

        /* Set up of the initial working grid */
        var x = minX
        while (x < maxX) {
            var y = minY
            while (y < maxY) {
                val centerX = x + (halfCellSize)
                val centerY = y + (halfCellSize)
                val distance = pointToPolygonDist(Point(centerX, centerY), polygon)
                queue.add(Cell(centerX, centerY, halfCellSize, distance))
                y += cellSize
            }
            x += cellSize
        }

        /* Heart of the beast : processing where is the visual center */
        var best = queue.poll()!!
        val centroid = centroid(polygon)
        val centroidCell = Cell(centroid.x, centroid.y, 0.0, pointToPolygonDist(centroid, polygon))
        if (centroidCell.distance > best.distance) best = centroidCell

        while (!queue.isEmpty) {
            val cell = queue.poll()

            if (cell.distance > best.distance) {
                best = cell
            }

            if (cell.max - best.distance <= precision) continue

            val half = cell.half / 2
            val children = listOf(
                Cell(cell.centerX - half, cell.centerY - half, half, pointToPolygonDist(Point(cell.centerX - half, cell.centerY - half), polygon)),
                Cell(cell.centerX + half, cell.centerY - half, half, pointToPolygonDist(Point(cell.centerX + half, cell.centerY - half), polygon)),
                Cell(cell.centerX - half, cell.centerY + half, half, pointToPolygonDist(Point(cell.centerX - half, cell.centerY + half), polygon)),
                Cell(cell.centerX + half, cell.centerY + half, half, pointToPolygonDist(Point(cell.centerX + half, cell.centerY + half), polygon))
            )
            for (c in children) {
                queue.add(c)
            }
        }

        return Point(best.centerX, best.centerY)
    }

    fun pointToPolygonDist(pointToObserve: Point, polygon: Polygon): Double {
        var inside = false
        var minDistSq = Double.POSITIVE_INFINITY

        val ring = polygon.shape
        for (i in ring.indices) {
            val pointA = ring[i]
            val pointB = ring[(i + 1) % ring.size]

            val distSq = pointToSegmentDistSq(pointToObserve, pointA, pointB)
            if (distSq < minDistSq) minDistSq = distSq

            val interesects = ((pointA.y > pointToObserve.y) != (pointB.y > pointToObserve.y)) && (pointToObserve.x < (pointB.x - pointA.x) * (pointToObserve.y - pointA.y) / (pointB.y - pointA.y) + pointA.x)
            if (interesects) inside = !inside
        }

        val dist = kotlin.math.sqrt(minDistSq)
        return if (inside) dist else -dist
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
}
