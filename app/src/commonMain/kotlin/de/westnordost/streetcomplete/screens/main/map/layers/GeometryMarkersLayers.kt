package de.westnordost.streetcomplete.screens.main.map.layers

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.preset_maki_circle
import de.westnordost.streetcomplete.screens.main.map.byZoom
import de.westnordost.streetcomplete.screens.main.map.isArea
import de.westnordost.streetcomplete.screens.main.map.isPoint
import de.westnordost.streetcomplete.screens.main.map.toGeometry
import de.westnordost.streetcomplete.screens.main.map.toPosition
import de.westnordost.streetcomplete.ui.common.quest.Marker
import de.westnordost.streetcomplete.ui.ktx.id
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.maplibre.compose.expressions.ast.Expression
import org.maplibre.compose.expressions.dsl.condition
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.convertToString
import org.maplibre.compose.expressions.dsl.eq
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.dsl.offset
import org.maplibre.compose.expressions.dsl.switch
import org.maplibre.compose.expressions.value.ImageValue
import org.maplibre.compose.expressions.value.LineCap
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
import org.maplibre.spatialk.geojson.Point

private val GeometryMarkerColor = Color(0xffd140d0)

/** Displays optional icons/titles and the geometry of elements surrounding a selected quest. */
@Composable
@MaplibreComposable
fun GeometryMarkersLayers(markers: Collection<Marker>) {
    val features = markers.flatMap { it.toGeometryMarkerFeatures() }
    // TODO(maplibre-compose): Restore the legacy source's volatile flag when GeoJsonOptions exposes it.
    val source = rememberGeoJsonSource(
        data = GeoJsonData.Features(FeatureCollection(features))
    )

    FillLayer(
        id = "geo-fill",
        source = source,
        filter = feature.isArea(),
        opacity = const(0.3f),
        color = const(GeometryMarkerColor),
    )
    LineLayer(
        id = "geo-lines",
        source = source,
        opacity = const(0.5f),
        color = const(GeometryMarkerColor),
        width = const(10.dp),
        cap = const(LineCap.Round),
    )
    SymbolLayer(
        id = "geo-symbols",
        source = source,
        filter = feature.isPoint(),
        iconImage = markerIconExpression(markers),
        iconColor = const(GeometryMarkerColor),
        iconSize = byZoom(17 to 0.5f, 19 to 1f),
        iconAllowOverlap = const(true),
        textField = feature["label"].convertToString(),
        textColor = const(GeometryMarkerColor),
        textSize = const(16.sp),
        textFont = const(listOf("Roboto Bold")),
        textAnchor = const(SymbolAnchor.Top),
        textOffset = offset(0.em, 1.em),
        textOptional = const(true),
    )
}

@Composable
private fun markerIconExpression(markers: Collection<Marker>): Expression<ImageValue> {
    val fallback = Res.drawable.preset_maki_circle
    val conditions = markers.mapNotNull(Marker::icon).distinct().mapNotNull { resource ->
        val id = resource.id ?: return@mapNotNull null
        condition(
            test = feature["icon"].convertToString() eq const(id),
            output = markerImage(resource, id),
        )
    }
    return switch(
        *conditions.toTypedArray(),
        fallback = markerImage(fallback, fallback.id.orEmpty()),
    )
}

@Composable
private fun markerImage(resource: DrawableResource, id: String): Expression<ImageValue> =
    image(painterResource(resource), drawAsSdf = id.startsWith("preset_"))

internal fun Marker.toGeometryMarkerFeatures(): List<Feature<Geometry, JsonObject>> = buildList {
    if (icon != null || title != null || geometry is ElementPointGeometry) {
        val properties = mutableMapOf<String, JsonElement>(
            "icon" to JsonPrimitive(icon?.id ?: "preset_maki_circle")
        )
        title?.let { properties["label"] = JsonPrimitive(it) }
        add(Feature(Point(geometry.center.toPosition()), JsonObject(properties)))
    }

    if (geometry is ElementPolygonsGeometry || geometry is ElementPolylinesGeometry) {
        add(Feature(geometry.toGeometry(), JsonObject(emptyMap())))
    }
}
