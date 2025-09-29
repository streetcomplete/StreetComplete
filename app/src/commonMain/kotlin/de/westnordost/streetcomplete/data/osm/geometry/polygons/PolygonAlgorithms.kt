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
        // TODO: Implement polylabel algorithm
        return centroid(polygon)
    }
}
