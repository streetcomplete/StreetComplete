package de.westnordost.streetcomplete.screens.main.map2.style

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.screens.main.map2.toGeoJson
import de.westnordost.streetcomplete.ui.theme.GeometryMarker
import dev.sargunv.maplibrecompose.compose.MaplibreComposable
import dev.sargunv.maplibrecompose.compose.layer.FillLayer
import dev.sargunv.maplibrecompose.compose.layer.LineLayer
import dev.sargunv.maplibrecompose.compose.layer.SymbolLayer
import dev.sargunv.maplibrecompose.compose.source.rememberGeoJsonSource
import dev.sargunv.maplibrecompose.core.source.GeoJsonData
import dev.sargunv.maplibrecompose.expressions.dsl.any
import dev.sargunv.maplibrecompose.expressions.dsl.asString
import dev.sargunv.maplibrecompose.expressions.dsl.const
import dev.sargunv.maplibrecompose.expressions.dsl.Feature
import dev.sargunv.maplibrecompose.expressions.dsl.offset
import dev.sargunv.maplibrecompose.expressions.value.LineCap
import dev.sargunv.maplibrecompose.expressions.value.LineJoin
import dev.sargunv.maplibrecompose.expressions.value.SymbolAnchor
import io.github.dellisd.spatialk.geojson.FeatureCollection
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

data class Marker(
    val geometry: ElementGeometry,
    /** drawable resource name */
    val icon: String? = null,
    val title: String? = null
)

@MaplibreComposable @Composable
fun GeometryMarkersLayers(markers: Collection<Marker>) {
    val features = FeatureCollection(markers.flatMap { it.toGeoJsonFeature() })
    val source = rememberGeoJsonSource(
        id = "geometry-source",
        data = GeoJsonData.Features(features)
    )

    FillLayer(
        id = "geo-fill",
        source = source,
        filter = Feature.isArea(),
        opacity = const(0.3f),
        color = const(GeometryMarker),
    )
    LineLayer(
        id = "geo-lines",
        source = source,
        filter = any(Feature.isArea(), Feature.isLines()),
        opacity = const(0.5f),
        color = const(GeometryMarker),
        width = const(10.dp),
        cap = const(LineCap.Round),
        join = const(LineJoin.Round)
    )
    SymbolLayer(
        id = "geo-symbols",
        source = source,
        filter = Feature.isPoint(),
        iconImage = Feature.get("icon"), // TODO get icon!!
        iconSize = byZoom(17 to 0.5f, 19 to 1f),
        iconAllowOverlap = const(true),
        textField = Feature.get("label").asString(),
        textColor = const(GeometryMarker),
        textSize = const(16.sp),
        textFont = const(listOf("Roboto Bold")),
        textAnchor = const(SymbolAnchor.Top),
        textOffset = offset(0.em, 1.em),
        textOptional = const(true),
    )
}

private typealias GeoJsonFeature = io.github.dellisd.spatialk.geojson.Feature

private fun Marker.toGeoJsonFeature(): List<GeoJsonFeature> {
    val features = ArrayList<GeoJsonFeature>(3)
    // point marker or any marker with title or icon
    if (icon != null || title != null || geometry is ElementPointGeometry) {
        val p = HashMap<String, JsonElement>(2)

        // TODO some icons should be sdf, others, not
        //   val sdf = name.startsWith("preset_")
        val mustHaveIcon = icon ?: "preset_maki_circle"
        p["icon"] = JsonPrimitive(mustHaveIcon)
        if (title != null) {
            p["label"] = JsonPrimitive(title)
        }
        features.add(GeoJsonFeature(geometry.toGeoJson(), p))
    }

    // polygon / polylines marker(s)
    if (geometry is ElementPolygonsGeometry || geometry is ElementPolylinesGeometry) {
        features.add(GeoJsonFeature(geometry.toGeoJson()))
    }
    return features
}
