package de.westnordost.streetcomplete.data.osm.geometry.polygons

/**
 * Representation of a polygon with
 * a list of points that represents the outer shape
 * (optional) a list composed of lists of points that each represents a hole in the polygon
 */
class Polygon (val shape: List<Point>, val holes: List<List<Point>> = emptyList()) {
}
