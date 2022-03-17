package de.westnordost.streetcomplete.data.osm.geometry

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

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
 *  */
class PolylinesSerializer {

    fun serialize(polylines: List<List<LatLon>>): ByteArray {
        val baos = ByteArrayOutputStream()
        ObjectOutputStream(baos).use { oos ->
            oos.writeInt(polylines.size)
            for (polyline in polylines) {
                oos.writeInt(polyline.size)
                for (position in polyline) {
                    oos.writeDouble(position.latitude)
                    oos.writeDouble(position.longitude)
                }
            }
        }
        return baos.toByteArray()
    }

    fun deserialize(byteArray: ByteArray): List<List<LatLon>> {
        val bais = ByteArrayInputStream(byteArray)
        return ObjectInputStream(bais).use { ois ->
            MutableList(ois.readInt()) {
                MutableList(ois.readInt()) {
                    LatLon(ois.readDouble(), ois.readDouble())
                }
            }
        }
    }
}
