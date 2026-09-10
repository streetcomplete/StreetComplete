package de.westnordost.streetcomplete.screens.main.map.layers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.preset_maki_circle
import de.westnordost.streetcomplete.screens.main.map.byZoom
import de.westnordost.streetcomplete.screens.main.map.isArea
import de.westnordost.streetcomplete.screens.main.map.isPoint
import de.westnordost.streetcomplete.screens.main.map.mapIconImage
import de.westnordost.streetcomplete.ui.ktx.id
import de.westnordost.streetcomplete.ui.theme.GeometryMarker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import org.maplibre.compose.expressions.dsl.case
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.convertToString
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.dsl.not
import org.maplibre.compose.expressions.dsl.switch
import org.maplibre.compose.expressions.dsl.textOffset
import org.maplibre.compose.expressions.value.LineCap
import org.maplibre.compose.expressions.value.LineJoin
import org.maplibre.compose.expressions.value.SymbolAnchor
import org.maplibre.compose.layers.FillLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.util.MaplibreComposable
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry

/** Displays some generic geometry markers with an optional icon on the map. This is used to
 *  show the geometry of elements surrounding the selected quest */
@MaplibreComposable
@Composable
fun GeometryMarkersLayers(markers: Collection<Marker>) {
    val features by produceState<List<Feature<Geometry, JsonObject>>>(emptyList(), markers) {
        value = withContext(Dispatchers.Default) { markers.flatMap { it.toGeoJsonFeature() } }
    }
    val source = rememberGeoJsonSource(
        data = GeoJsonData.Features(FeatureCollection(features))
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
        filter = !feature.isPoint(),
        opacity = const(0.5f),
        color = const(Color.GeometryMarker),
        width = const(10.dp),
        cap = const(LineCap.Round),
        join = const(LineJoin.Round)
    )
    val markerImages = markers.map { it.icon ?: Res.drawable.preset_maki_circle }.distinct().map { icon ->
        case("marker_" + icon.id, mapIconImage(icon))
    }
    SymbolLayer(
        id = "geo-symbols",
        source = source,
        filter = feature.isPoint(),
        iconImage = switch(feature["icon"].convertToString(), markerImages, image("")),
        iconColor = const(Color.GeometryMarker),
        iconSize = byZoom(17 to 0.5f, 19 to 1f),
        iconAllowOverlap = const(true),
        textField = feature["label"].convertToString(),
        textColor = const(Color.GeometryMarker),
        textSize = const(16.sp),
        textFont = const(listOf("Roboto Bold")),
        textAnchor = const(SymbolAnchor.Top),
        textOffset = textOffset(0.em, 1.em),
        textOptional = const(true),
    )
}
