package de.westnordost.streetcomplete.screens.main.map.layers

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.screens.main.map.byZoom
import de.westnordost.streetcomplete.screens.main.map.isArea
import de.westnordost.streetcomplete.screens.main.map.isLines
import de.westnordost.streetcomplete.screens.main.map.isPoint
import de.westnordost.streetcomplete.screens.main.map.toGeometry
import de.westnordost.streetcomplete.ui.ktx.id
import de.westnordost.streetcomplete.ui.theme.GeometryMarker
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.jetbrains.compose.resources.DrawableResource
import org.maplibre.compose.expressions.ast.Expression
import org.maplibre.compose.expressions.dsl.any
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.convertToString
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.dsl.offset
import org.maplibre.compose.expressions.value.LineCap
import org.maplibre.compose.expressions.value.LineJoin
import org.maplibre.compose.expressions.value.StringValue
import org.maplibre.compose.expressions.value.SymbolAnchor
import org.maplibre.compose.layers.FillLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.util.MaplibreComposable
import org.maplibre.spatialk.geojson.Geometry

/** Displays some generic geometry markers with an optional icon on the map. This is used to
 *  show the geometry of elements surrounding the selected quest */
@MaplibreComposable
@Composable
fun GeometryMarkersLayers(markers: Collection<Marker>) {
    val source = rememberGeoJsonSource(
        data = GeoJsonData.Features(FeatureCollection(markers.flatMap { it.toGeoJsonFeature() }))
    )

    FillLayer(
        id = "geo-fill",
        source = source,
        filter = feature.isArea(),
        opacity = const(0.3f),
        color = const(Color.GeometryMarker),
    )
    LineLayer(
        id = "geo-lines",
        source = source,
        filter = any(feature.isArea(), feature.isLines()),
        opacity = const(0.5f),
        color = const(Color.GeometryMarker),
        width = const(10.dp),
        cap = const(LineCap.Round),
        join = const(LineJoin.Round)
    )
    SymbolLayer(
        id = "geo-symbols",
        source = source,
        filter = feature.isPoint(),
        iconImage = image(feature["icon"].convertToString()),
        iconSize = byZoom(17 to 0.5f, 19 to 1f),
        iconAllowOverlap = const(true),
        textField = feature["label"].convertToString(),
        textColor = const(Color.GeometryMarker),
        textSize = const(16.sp),
        textFont = const(listOf("Roboto Bold")),
        textAnchor = const(SymbolAnchor.Top),
        textOffset = offset(0.em, 1.em),
        textOptional = const(true),
    )
}

data class Marker(
    val geometry: ElementGeometry,
    /** drawable resource name */
    val icon: DrawableResource? = null,
    val title: String? = null
)

private fun Marker.toGeoJsonFeature(): List<Feature<Geometry, JsonObject>> {
    val features = ArrayList<Feature<Geometry, JsonObject>>(3)
    // point marker or any marker with title or icon
    if (icon != null || title != null || geometry is ElementPointGeometry) {
        val p = HashMap<String, JsonElement>(2)

        p["icon"] = JsonPrimitive(icon?.id ?: "preset_maki_circle")
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
