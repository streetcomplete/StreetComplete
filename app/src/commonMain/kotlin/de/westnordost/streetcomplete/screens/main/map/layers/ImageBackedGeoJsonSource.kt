package de.westnordost.streetcomplete.screens.main.map.layers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.serialization.json.JsonObject
import org.maplibre.compose.map.MapState
import org.maplibre.compose.map.StyleLoadState
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.GeoJsonOptions
import org.maplibre.compose.sources.GeoJsonSource
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.util.MaplibreComposable
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry

/**
 * Keeps the declarative source empty until every image referenced by [data] is installed, then
 * publishes updates through Compose. The source remains declaratively owned; its loaded handle
 * cannot be used to change its definition.
 */
@Composable
@MaplibreComposable
internal fun rememberImageBackedGeoJsonSource(
    mapState: MapState,
    data: GeoJsonData,
    imageRegistry: DynamicStyleImageRegistry,
    requiredImageIds: Set<String>,
    options: GeoJsonOptions = GeoJsonOptions(),
): GeoJsonSource {
    val installedImages by imageRegistry.installedImages.collectAsState()
    val imagesReady =
        mapState.style.loadState == StyleLoadState.Ready &&
            installedImages.containsAll(requiredImageIds)
    return rememberGeoJsonSource(if (imagesReady) data else EMPTY_IMAGE_BACKED_DATA, options)
}

private val EMPTY_IMAGE_BACKED_DATA =
    GeoJsonData.Features(FeatureCollection<Geometry, JsonObject>(emptyList()))
