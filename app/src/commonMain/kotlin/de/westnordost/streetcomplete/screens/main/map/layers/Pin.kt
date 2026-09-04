package de.westnordost.streetcomplete.screens.main.map.layers

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.screens.main.map.toGeometry
import de.westnordost.streetcomplete.ui.ktx.id
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.jetbrains.compose.resources.DrawableResource
import org.maplibre.spatialk.geojson.Feature

/** Intermediate data structure for display of pins on the map. The [icon] of a Pin shall be drawn
 *  with a [PinPainter][de.westnordost.streetcomplete.screens.main.map.PinPainter] */
data class Pin(
    val position: LatLon,
    val icon: DrawableResource,
    val properties: JsonObject? = null,
    val order: Int = 0
)

fun Pin.toGeoJsonFeature() =
    Feature(
        geometry = position.toGeometry(),
        properties =
            JsonObject(
                mapOf(
                    "icon-image" to JsonPrimitive("pin_" + icon.id),
                    "icon-order" to JsonPrimitive(order + 50),
                )
                    + (properties as Map<String, JsonElement>)
            )
    )
