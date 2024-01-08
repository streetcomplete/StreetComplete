package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.streetcomplete.util.math.normalizeLongitude
import kotlinx.serialization.Serializable

@Serializable
data class BoundingBox(val min: LatLon, val max: LatLon) {
    constructor(
        minLatitude: Double,
        minLongitude: Double,
        maxLatitude: Double,
        maxLongitude: Double
    ) : this(LatLon(minLatitude, minLongitude), LatLon(maxLatitude, maxLongitude))

    init {
        require(min.latitude <= max.latitude) {
            "Min latitude ${min.latitude} is greater than max latitude ${max.latitude}"
        }
    }

    val crosses180thMeridian get() =
        normalizeLongitude(min.longitude) > normalizeLongitude(max.longitude)
}

/** @return two new bounds split alongside the 180th meridian or, if these bounds do not cross
 * the 180th meridian, just this
 */
fun BoundingBox.splitAt180thMeridian(): List<BoundingBox> {
    return if (crosses180thMeridian) {
        listOf(
            // - 1e-13 because the two bboxes should not intersect. I.e. we want the last possible
            // value before it wraps around to -180.0
            // (1e-13 is the maximum decimal precision for when there is 180 before the point)
            BoundingBox(min.latitude, min.longitude, max.latitude, 180.0 - 1e-13),
            BoundingBox(min.latitude, -180.0, max.latitude, max.longitude)
        )
    } else {
        listOf(this)
    }
}

/** @return a polygon with the same extent as this bounding box, defined in counter-clockwise order
 * */
fun BoundingBox.toPolygon() = listOf(
    min,
    LatLon(min.latitude, max.longitude),
    max,
    LatLon(max.latitude, min.longitude),
    min,
)
