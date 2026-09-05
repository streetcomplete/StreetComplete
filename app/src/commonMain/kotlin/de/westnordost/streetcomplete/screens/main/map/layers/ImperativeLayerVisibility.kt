package de.westnordost.streetcomplete.screens.main.map.layers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import org.maplibre.compose.map.MapState
import org.maplibre.compose.map.StyleLoadState
import org.maplibre.compose.util.MaplibreComposable

/**
 * Keeps transient visibility out of declarative layer definitions.
 *
 * MapLibre's native property mutation is synchronous. Running a multi-layer visibility change on
 * the Compose dispatcher therefore blocks UI frames while the map-owner thread processes each
 * command. This retains the layers, as master's map components do, and dispatches the batch away
 * from the UI thread.
 */
@Composable
@MaplibreComposable
internal fun ImperativeLayerVisibility(
    mapState: MapState,
    layerIds: List<String>,
    visible: Boolean,
) {
    val currentVisible = rememberUpdatedState(visible)
    LaunchedEffect(mapState, layerIds) {
        var appliedVisibility: Boolean? = null
        snapshotFlow {
            Triple(
                mapState.style.loadState,
                layerIds.all { mapState.style.layers[it] != null },
                currentVisible.value,
            )
        }
            .distinctUntilChanged()
            .collectLatest { (loadState, allLayersInstalled, showLayers) ->
                if (loadState != StyleLoadState.Ready || !allLayersInstalled) {
                    appliedVisibility = null
                    return@collectLatest
                }
                // A newly declared MapLibre layer is visible by default. Avoid a redundant
                // owner-thread round trip until the first actual visibility transition.
                if (appliedVisibility == null && showLayers) {
                    appliedVisibility = true
                    return@collectLatest
                }
                if (appliedVisibility == showLayers) return@collectLatest
                val handles = layerIds.map { id -> mapState.style.layers[id] ?: return@collectLatest }
                try {
                    withContext(Dispatchers.Default) {
                        val value = JsonPrimitive(if (showLayers) "visible" else "none")
                        handles.forEach { it.setLayoutProperty("visibility", value) }
                    }
                    appliedVisibility = showLayers
                } catch (error: IllegalStateException) {
                    // A loaded-style transition changes the observed state and retries the batch.
                    if (!error.isStyleHandleRace()) throw error
                }
            }
    }
}

/** Applies a transient paint value to retained layers without blocking the Compose dispatcher. */
@Composable
@MaplibreComposable
internal fun ImperativeLayerPaintProperty(
    mapState: MapState,
    layerIds: List<String>,
    property: String,
    value: JsonElement,
    defaultValue: JsonElement,
) {
    val currentValue = rememberUpdatedState(value)
    LaunchedEffect(mapState, layerIds, property) {
        var appliedValue: JsonElement? = null
        snapshotFlow {
            Triple(
                mapState.style.loadState,
                layerIds.all { mapState.style.layers[it] != null },
                currentValue.value,
            )
        }
            .distinctUntilChanged()
            .collectLatest { (loadState, allLayersInstalled, propertyValue) ->
                if (loadState != StyleLoadState.Ready || !allLayersInstalled) {
                    appliedValue = null
                    return@collectLatest
                }
                if (appliedValue == null && propertyValue == defaultValue) {
                    appliedValue = defaultValue
                    return@collectLatest
                }
                if (appliedValue == propertyValue) return@collectLatest
                val handles = layerIds.map { id -> mapState.style.layers[id] ?: return@collectLatest }
                try {
                    withContext(Dispatchers.Default) {
                        handles.forEach { it.setPaintProperty(property, propertyValue) }
                    }
                    appliedValue = propertyValue
                } catch (error: IllegalStateException) {
                    // A loaded-style transition changes the observed state and retries the batch.
                    if (!error.isStyleHandleRace()) throw error
                }
            }
    }
}
