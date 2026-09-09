package de.westnordost.streetcomplete.screens.main.map.layers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.preset_maki_circle
import de.westnordost.streetcomplete.screens.main.map.isArea
import de.westnordost.streetcomplete.screens.main.map.isPoint
import de.westnordost.streetcomplete.screens.main.map.toGeometry
import de.westnordost.streetcomplete.screens.main.map.toPosition
import de.westnordost.streetcomplete.ui.common.quest.Marker
import de.westnordost.streetcomplete.ui.theme.GeometryMarker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.convertToString
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.dsl.interpolate
import org.maplibre.compose.expressions.dsl.linear
import org.maplibre.compose.expressions.dsl.not
import org.maplibre.compose.expressions.dsl.textOffset
import org.maplibre.compose.expressions.dsl.zoom
import org.maplibre.compose.expressions.value.LineCap
import org.maplibre.compose.expressions.value.SymbolAnchor
import org.maplibre.compose.layers.FillLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.map.MapState
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.util.MaplibreComposable
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.Point

/** Displays the line and polygon geometry of elements surrounding a selected quest. */
@Composable
@MaplibreComposable
internal fun GeometryMarkersLayers(
    mapState: MapState,
    markers: Collection<Marker>,
    imageRegistry: DynamicStyleImageRegistry,
) {
    val markerResources = markers.map { it.icon ?: Res.drawable.preset_maki_circle }.distinct()
    val images = rememberPlainStyleImages(markerResources)
    RegisterDynamicStyleImages(imageRegistry, "geometry-markers", images)
    val data by
        produceState<GeoJsonData>(
            EMPTY_GEOMETRY_MARKERS_DATA,
            markers,
        ) {
            value =
                withContext(Dispatchers.Default) {
                    if (markers.isEmpty()) {
                        EMPTY_GEOMETRY_MARKERS_DATA
                    } else {
                        GeoJsonData.Features(
                            FeatureCollection(markers.flatMap(Marker::toGeometryMarkerFeatures))
                        )
                    }
                }
        }
    val source =
        rememberImageBackedGeoJsonSource(
            mapState = mapState,
            data = data,
            imageRegistry = imageRegistry,
            requiredImageIds = images.mapTo(mutableSetOf(), DynamicStyleImage::id),
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
    )
    SymbolLayer(
        id = "geo-symbols",
        source = source,
        filter = feature.isPoint(),
        iconImage = image(feature["icon-image"].convertToString()),
        iconSize =
            interpolate(
                linear(),
                zoom(),
                17 to const(0.5f),
                19 to const(1f),
            ),
        iconAllowOverlap = const(true),
        iconColor = const(Color.GeometryMarker),
        textField = feature["label"].convertToString(),
        textColor = const(Color.GeometryMarker),
        textSize = const(16.sp),
        textFont = const(listOf("Roboto Bold")),
        textAnchor = const(SymbolAnchor.Top),
        textOffset = textOffset(0.em, 1.em),
        textOptional = const(true),
    )
}

internal fun Marker.toGeometryMarkerFeatures(): List<Feature<Geometry, JsonObject>> = buildList {
    if (icon != null || title != null || geometry is ElementPointGeometry) {
        val resource = icon ?: Res.drawable.preset_maki_circle
        add(
            Feature(
                Point(geometry.center.toPosition()),
                JsonObject(
                    buildMap {
                        put("icon-image", JsonPrimitive(plainStyleImageId(resource)))
                        title?.let { put("label", JsonPrimitive(it)) }
                    }
                ),
            )
        )
    }
    if (geometry is ElementPolygonsGeometry || geometry is ElementPolylinesGeometry) {
        add(Feature(geometry.toGeometry(), JsonObject(emptyMap())))
    }
}

private val EMPTY_GEOMETRY_MARKERS_DATA =
    GeoJsonData.Features(FeatureCollection<Geometry, JsonObject>(emptyList()))
