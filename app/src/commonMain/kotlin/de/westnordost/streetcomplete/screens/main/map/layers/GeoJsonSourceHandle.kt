package de.westnordost.streetcomplete.screens.main.map.layers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import org.maplibre.compose.map.MapState
import org.maplibre.compose.map.StyleLoadState
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.GeoJsonOptions
import org.maplibre.compose.sources.GeoJsonSource
import org.maplibre.compose.sources.GeoJsonSourceHandle
import org.maplibre.compose.util.MaplibreComposable
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry

/** Observes the loaded-source registry only until this source becomes available. */
internal suspend fun MapState.awaitGeoJsonSource(id: String): GeoJsonSourceHandle =
    snapshotFlow { style.sources[id] as? GeoJsonSourceHandle }.filterNotNull().first()

/**
 * Keeps a local GeoJSON source installed while publishing changing data through its loaded-style
 * handle. This mirrors master's long-lived map components and avoids a full style reconciliation
 * for data-only updates.
 */
@Composable
@MaplibreComposable
internal fun rememberImperativeGeoJsonSource(
    mapState: MapState,
    id: String,
    data: GeoJsonData,
    options: GeoJsonOptions = GeoJsonOptions(),
    imageRegistry: DynamicStyleImageRegistry? = null,
    requiredImageIds: Set<String> = emptySet(),
): GeoJsonSource {
    require(requiredImageIds.isEmpty() || imageRegistry != null)
    val source = remember(id, options) { GeoJsonSource(id, EMPTY_GEOJSON_DATA, options) }
    val currentData = rememberUpdatedState(data)
    val currentRequiredImageIds = rememberUpdatedState(requiredImageIds)
    LaunchedEffect(mapState, id) {
        snapshotFlow {
            Triple(
                mapState.style.loadState,
                currentData.value,
                currentRequiredImageIds.value,
            )
        }
            .distinctUntilChanged()
            .collectLatest { (loadState, sourceData, imageIds) ->
                if (loadState != StyleLoadState.Ready) return@collectLatest
                val sourceHandle = mapState.style.sources[id] as? GeoJsonSourceHandle
                    ?: return@collectLatest
                try {
                    imageRegistry?.awaitInstalled(imageIds)
                    withContext(Dispatchers.Default) {
                        sourceHandle.setData(sourceData)
                    }
                } catch (error: IllegalStateException) {
                    // A loaded-style transition changes loadState and replays the current data.
                    if (!error.isStyleHandleRace()) throw error
                }
            }
    }
    return source
}

private val EMPTY_GEOJSON_DATA = GeoJsonData.Features(
    FeatureCollection<Geometry, JsonObject>(emptyList())
)
