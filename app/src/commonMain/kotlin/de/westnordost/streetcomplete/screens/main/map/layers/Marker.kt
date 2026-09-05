package de.westnordost.streetcomplete.screens.main.map.layers

import androidx.compose.ui.graphics.Color
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.screens.main.map.toGeometry
import de.westnordost.streetcomplete.ui.ktx.id
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.jetbrains.compose.resources.DrawableResource
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Geometry
import kotlin.collections.set

/** Intermediate data structure for a geometry marker. The [icon] of a geometry marker shall be
 *  drawn with a [ColorFilterPainter][de.westnordost.streetcomplete.ui.util.ColorFilterPainter] in
 *  the color [Color.GeometryMarker] */
data class Marker(
    val geometry: ElementGeometry,
    /** drawable resource name */
    val icon: DrawableResource? = null,
    val title: String? = null
)

fun Marker.toGeoJsonFeature(): List<Feature<Geometry, JsonObject>> {
    val features = ArrayList<Feature<Geometry, JsonObject>>(3)
    // point marker or any marker with title or icon
    if (icon != null || title != null || geometry is ElementPointGeometry) {
        val p = HashMap<String, JsonElement>(2)

        p["icon"] = JsonPrimitive("marker_" + (icon?.id ?: "preset_maki_circle"))
        if (title != null) {
            p["label"] = JsonPrimitive(title)
        }
        features.add(Feature(geometry.toGeometry(), JsonObject(p)))
    }

    // polygon / polylines marker(s)
    if (geometry is ElementPolygonsGeometry || geometry is ElementPolylinesGeometry) {
        features.add(Feature(geometry.toGeometry(), JsonObject(emptyMap())))
    }
    return features
}
