package de.westnordost.streetcomplete.data.osm.geometry.polygons

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
        val length = maxX - minX
        val height = maxY - minY
        val cellSize = minOf(length, height)
        val h = cellSize / 2.0

        val queue = PriorityQueue<Cell>()
    }
}
