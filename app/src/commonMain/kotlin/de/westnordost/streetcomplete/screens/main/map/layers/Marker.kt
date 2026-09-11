package de.westnordost.streetcomplete.screens.main.map.layers

import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.screens.main.map.toGeometry
import de.westnordost.streetcomplete.ui.common.quest.Marker
import de.westnordost.streetcomplete.ui.ktx.id
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Geometry
import kotlin.collections.set

fun Marker.toGeoJsonFeature(): List<Feature<Geometry, JsonObject>> {
    val features = ArrayList<Feature<Geometry, JsonObject>>(3)
    // point marker or any marker with title or icon
    if (icon != null || title != null || geometry is ElementPointGeometry) {
        val p = HashMap<String, JsonElement>(2)

        p["icon"] = JsonPrimitive("marker_" + (icon?.id ?: "preset_maki_circle"))
        if (title != null) {
            p["label"] = JsonPrimitive(title)
        }
        features.add(Feature(geometry.center.toGeometry(), JsonObject(p)))
    }

    // polygon / polylines marker(s)
    if (geometry is ElementPolygonsGeometry || geometry is ElementPolylinesGeometry) {
        features.add(Feature(geometry.toGeometry(), JsonObject(emptyMap())))
    }
    return features
}
