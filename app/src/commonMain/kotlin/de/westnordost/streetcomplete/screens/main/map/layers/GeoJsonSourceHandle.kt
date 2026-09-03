package de.westnordost.streetcomplete.screens.main.map.layers

import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import org.maplibre.compose.map.MapState
import org.maplibre.compose.sources.GeoJsonSourceHandle

/** Observes the loaded-source registry only until this source becomes available. */
internal suspend fun MapState.awaitGeoJsonSource(id: String): GeoJsonSourceHandle =
    snapshotFlow { style.sources[id] as? GeoJsonSourceHandle }.filterNotNull().first()
