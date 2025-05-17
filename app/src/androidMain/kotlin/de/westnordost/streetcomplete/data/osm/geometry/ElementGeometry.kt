package de.westnordost.streetcomplete.data.osm.geometry

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Information on the geometry of a quest  */
@Serializable
sealed class ElementGeometry {
    abstract val center: LatLon
    // the bbox should not be serialized, his is why the bounds cannot be a (computed) property directly
    abstract fun getBounds(): BoundingBox
}

@Serializable
@SerialName("polylines")
data class ElementPolylinesGeometry(val polylines: List<List<LatLon>>, override val center: LatLon) : ElementGeometry() {
    private val bbox by lazy { polylines.flatten().enclosingBoundingBox() }
    override fun getBounds(): BoundingBox = bbox
}

@Serializable
@SerialName("polygons")
data class ElementPolygonsGeometry(val polygons: List<List<LatLon>>, override val center: LatLon) : ElementGeometry() {
    private val bbox by lazy { polygons.flatten().enclosingBoundingBox() }
    override fun getBounds(): BoundingBox = bbox
}

@Serializable
@SerialName("point")
data class ElementPointGeometry(override val center: LatLon) : ElementGeometry() {
    private val bbox by lazy { BoundingBox(center, center) }
    override fun getBounds(): BoundingBox = bbox
}
