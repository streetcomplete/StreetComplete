package de.westnordost.streetcomplete.data.osm

import java.io.Serializable

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.util.SphericalEarthMath.*

/** Information on the geometry of a quest  */
sealed class ElementGeometry : Serializable {
    abstract val center: LatLon
    abstract val bounds: BoundingBox
}

data class ElementPolylinesGeometry(val polylines: List<List<LatLon>>, override val center: LatLon) : ElementGeometry() {
    override val bounds: BoundingBox by lazy { enclosingBoundingBox(polylines.flatten()) }
}

data class ElementPolygonsGeometry(val polygons: List<List<LatLon>>, override val center: LatLon) : ElementGeometry() {
    override val bounds: BoundingBox by lazy { enclosingBoundingBox(polygons.flatten()) }
}

data class ElementPointGeometry(override val center: LatLon) : ElementGeometry() {
    override val bounds: BoundingBox by lazy { enclosingBoundingBox(listOf(center)) }
}
