package de.westnordost.streetcomplete.util.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.spatialk.geojson.Position

// TODO maplibre-compose: Use the upstream serializer once https://github.com/maplibre/maplibre-compose/issues/1389 is resolved.
object CameraPositionSerializer : KSerializer<CameraPosition> {
    override val descriptor = CameraPositionData.serializer().descriptor

    override fun serialize(encoder: Encoder, value: CameraPosition) {
        val data = CameraPositionData(value.bearing, value.target, value.tilt, value.zoom)
        encoder.encodeSerializableValue(CameraPositionData.serializer(), data)
    }

    override fun deserialize(decoder: Decoder): CameraPosition {
        val data = decoder.decodeSerializableValue(CameraPositionData.serializer())
        return CameraPosition(bearing = data.bearing, target = data.target, tilt = data.tilt, zoom = data.zoom)
    }
}

@Serializable
private data class CameraPositionData(
    val bearing: Double,
    val target: Position,
    val tilt: Double,
    val zoom: Double,
)
