package de.westnordost.streetcomplete.data.osm.geometry

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import kotlinx.io.readDouble
import kotlinx.io.writeDouble

/** Serializes a list of a list of latlons into to a byte array and back memory-efficiently.
 *
 *  Why have this when we could use Json.encodeToString etc. from kotlinx-serialization?
 *
 *  Because for the ElementGeometry, we only store these latlons, and many. If one
 *  serialized a list like this to Json, it would look like this...
 *
 *  `{"lat":"51.1324667","lon":"12.1345678"}`
 *
 *  ... for every single coordinate that is part of this particular geometry. That's more than 2
 *  times the size as when using this method.
 */
class PolylinesSerializer {

    fun serialize(polylines: List<List<LatLon>>): ByteArray {
        val buffer = Buffer()

        buffer.writeInt(polylines.size)
        for (polyline in polylines) {
            buffer.writeInt(polyline.size)
            for (position in polyline) {
                buffer.writeDouble(position.latitude)
                buffer.writeDouble(position.longitude)
            }
        }

        return buffer.readByteArray()
    }

    fun deserialize(byteArray: ByteArray): List<List<LatLon>> {
        val buffer = Buffer()
        buffer.write(byteArray)

        return MutableList(buffer.readInt()) {
            MutableList(buffer.readInt()) {
                LatLon(buffer.readDouble(), buffer.readDouble())
            }
        }
    }
}
