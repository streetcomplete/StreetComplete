@file:Suppress("TRANSIENT_IS_REDUNDANT")

package de.westnordost.streetcomplete.data.osm.geometry

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.lazy

/** Information on the geometry of a quest  */
@Serializable
sealed class ElementGeometry {
    abstract val center: LatLon
    @Transient abstract val bounds: BoundingBox
}

@Serializable
@SerialName("polylines")
data class ElementPolylinesGeometry(
    val polylines: List<List<LatLon>>,
    override val center: LatLon
) : ElementGeometry() {
    @Transient
    override val bounds: BoundingBox by lazy { polylines.flatten().enclosingBoundingBox() }
}

@Serializable
@SerialName("polygons")
data class ElementPolygonsGeometry(
    val polygons: List<List<LatLon>>,
    override val center: LatLon
) : ElementGeometry() {
    @Transient
    override val bounds: BoundingBox by lazy { polygons.flatten().enclosingBoundingBox() }
}

@Serializable
@SerialName("point")
data class ElementPointGeometry(override val center: LatLon) : ElementGeometry() {
    @Transient
    override val bounds : BoundingBox by lazy { BoundingBox(center, center) }
}
