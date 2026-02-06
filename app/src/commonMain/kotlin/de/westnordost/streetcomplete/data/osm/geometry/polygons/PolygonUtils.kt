package de.westnordost.streetcomplete.data.osm.geometry.polygons

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon

/* patch to bridge generic polygon logic to osm map logic */

object PolygonUtils {

    /* convert LatLon polygon to generic polygon */
    fun fromLatLon(shape: List<LatLon>, holes: List<List<LatLon>> = emptyList()): Polygon {
        val outerPts = shape.map { Point(it.longitude, it.latitude) }
        val holePts = holes.map { ring -> ring.map { Point (it.longitude, it.latitude) } }
        return Polygon(outerPts, holePts)
    }

    /* Provide a visual center (within the polygon) with the OSM LatLon logic */
    fun representativeCenter(polygon: Polygon): LatLon {
        val center = PolygonAlgorithms.polylabel(polygon)
        return LatLon(center.x, center.y)
    }
}
