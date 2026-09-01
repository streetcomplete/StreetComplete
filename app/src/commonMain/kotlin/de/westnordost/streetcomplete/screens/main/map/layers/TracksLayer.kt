package de.westnordost.streetcomplete.screens.main.map.layers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.track_nyan
import de.westnordost.streetcomplete.resources.track_nyan_record
import de.westnordost.streetcomplete.screens.main.map.animateLatLonAsState
import de.westnordost.streetcomplete.screens.main.map.toLineGeometry
import de.westnordost.streetcomplete.screens.main.map.toMultiLineGeometry
import de.westnordost.streetcomplete.util.ktx.isApril1st
import org.jetbrains.compose.resources.painterResource
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.value.LineCap
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.Source
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.util.MaplibreComposable
import org.maplibre.spatialk.geojson.GeometryCollection

/**
 * Displays completed track segments and the short, actively changing current segment.
 *
 * [trackpoints] should remain bounded (the legacy implementation retained at most 100 points) and
 * older chunks should be moved to [oldTrackpointLists]. This avoids copying an ever-growing track
 * into MapLibre for every location update.
 */
@Composable
@MaplibreComposable
fun TracksLayers(
    trackpoints: List<LatLon>,
    isRecording: Boolean,
    oldTrackpointLists: List<List<LatLon>>,
) {
    val lastSegment = remember(trackpoints) { trackpoints.takeLast(2).takeIf { it.size == 2 } }
    val trackWithoutLast = remember(trackpoints) { trackpoints.dropLast(1) }

    // TODO(maplibre-compose): Restore the legacy sources' volatile flags when GeoJsonOptions exposes it.
    val animatedSource = rememberGeoJsonSource(
        data = GeoJsonData.Features(
            lastSegment?.let { segment ->
                val animatedLastPosition by animateLatLonAsState(
                    targetValue = segment.last(),
                    initialValue = segment.first(),
                )
                listOf(segment.first(), animatedLastPosition).toLineGeometry()
            } ?: GeometryCollection(emptyList())
        )
    )
    val trackSource = rememberGeoJsonSource(
        data = GeoJsonData.Features(
            trackWithoutLast.toLineGeometry() ?: GeometryCollection(emptyList())
        )
    )
    val oldTrackSource = rememberGeoJsonSource(
        data = GeoJsonData.Features(oldTrackpointLists.toMultiLineGeometry())
    )

    // Preserve the legacy style ordering: the old track is nearest the label layers.
    TrackLayer("animate-track", animatedSource, isRecording = isRecording)
    TrackLayer("track", trackSource, isRecording = isRecording)
    TrackLayer("old-track", oldTrackSource, opacity = 0.2f)
}

@Composable
@MaplibreComposable
private fun TrackLayer(
    id: String,
    source: Source,
    isRecording: Boolean = false,
    opacity: Float = 0.6f,
) {
    val showAprilFoolsPattern = remember { isApril1st() }
    if (showAprilFoolsPattern) {
        AprilFoolsTrackLayer(id, source, isRecording, opacity)
    } else {
        DefaultTrackLayer(id, source, isRecording, opacity)
    }
}

@Composable
@MaplibreComposable
private fun AprilFoolsTrackLayer(
    id: String,
    source: Source,
    isRecording: Boolean,
    opacity: Float,
) {
    LineLayer(
        id = id,
        source = source,
        opacity = const(opacity),
        cap = const(LineCap.Round),
        dasharray = const(listOf(0f, 2f)),
        width = const(26.dp),
        pattern = image(painterResource(
            if (isRecording) Res.drawable.track_nyan_record else Res.drawable.track_nyan
        )),
    )
}

@Composable
@MaplibreComposable
private fun DefaultTrackLayer(
    id: String,
    source: Source,
    isRecording: Boolean,
    opacity: Float,
) {
    LineLayer(
        id = id,
        source = source,
        opacity = const(opacity),
        cap = const(LineCap.Round),
        dasharray = const(listOf(0f, 2f)),
        width = const(6.dp),
        color = const(if (isRecording) Color(0xfffe1616) else Color(0xff536dfe)),
    )
}
