package de.westnordost.streetcomplete.screens.main.map.layers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
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
import de.westnordost.streetcomplete.screens.main.map.toPosition
import de.westnordost.streetcomplete.ui.theme.Location
import de.westnordost.streetcomplete.util.ktx.isApril1st
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonPrimitive
import org.jetbrains.compose.resources.painterResource
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.value.LineCap
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.map.MapState
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.Source
import org.maplibre.compose.util.MaplibreComposable
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.toJson

/**
 * Displays completed track segments and the short, actively changing current segment.
 *
 * [trackpoints] should remain bounded (the legacy implementation retained at most 100 points) and
 * older chunks should be moved to [oldTrackpointsLists]. This avoids copying an ever-growing track
 * into MapLibre for every location update.
 */
@Composable
@MaplibreComposable
fun TracksLayers(
    mapState: MapState,
    trackpoints: List<LatLon>,
    isRecording: Boolean,
    oldTrackpointsLists: List<List<LatLon>>,
) {
    val showAprilFoolsPattern = remember { isApril1st() }
    val trackLastSegment = remember(trackpoints) {
        if (trackpoints.size >= 2) trackpoints.takeLast(2) else null
    }
    val trackWithoutLast = remember(trackpoints) {
        if (trackpoints.size > 1) trackpoints.take(trackpoints.size - 1) else emptyList()
    }

    val animatedData: GeoJsonData = if (trackLastSegment != null) {
        val segment = trackLastSegment
        val animatedLastPosition by animateLatLonAsState(
            targetValue = segment.last(),
            initialValue = segment.first(),
        )
        GeoJsonData.Features(
            LineString(listOf(segment.first().toPosition(), animatedLastPosition.toPosition()))
        )
    } else {
        EMPTY_TRACK_DATA
    }
    val trackData by produceState<GeoJsonData>(EMPTY_TRACK_DATA, trackWithoutLast) {
        value = withContext(Dispatchers.Default) {
            GeoJsonData.JsonString(
                trackWithoutLast.toLineGeometry()?.toJson() ?: EMPTY_TRACK_JSON
            )
        }
    }
    val oldTrackData by produceState<GeoJsonData>(EMPTY_TRACK_DATA, oldTrackpointsLists) {
        value = withContext(Dispatchers.Default) {
            val geometry = oldTrackpointsLists.toMultiLineGeometry()
            GeoJsonData.JsonString(
                if (geometry.coordinates.isEmpty()) EMPTY_TRACK_JSON else geometry.toJson()
            )
        }
    }

    val animatedSource = rememberImperativeGeoJsonSource(
        mapState = mapState,
        id = ANIMATED_TRACK_SOURCE_ID,
        data = animatedData,
    )
    val trackSource = rememberImperativeGeoJsonSource(
        mapState = mapState,
        id = TRACK_SOURCE_ID,
        data = trackData,
    )
    val oldTrackSource = rememberImperativeGeoJsonSource(
        mapState = mapState,
        id = OLD_TRACK_SOURCE_ID,
        data = oldTrackData,
    )

    if (!showAprilFoolsPattern) {
        ImperativeLayerPaintProperty(
            mapState = mapState,
            layerIds = ACTIVE_TRACK_LAYER_IDS,
            property = "line-color",
            value = JsonPrimitive(if (isRecording) "#fe1616" else "#536dfe"),
            defaultValue = JsonPrimitive("#536dfe"),
        )
    }
    TracksStyleLayers(
        animatedSource,
        trackSource,
        oldTrackSource,
        isRecording,
        showAprilFoolsPattern,
    )
}

@Composable
@MaplibreComposable
private fun TracksStyleLayers(
    animatedSource: Source,
    trackSource: Source,
    oldTrackSource: Source,
    isRecording: Boolean,
    showAprilFoolsPattern: Boolean,
) {
    // Preserve the legacy style ordering: the old track is nearest the label layers.
    TracksLayer(
        "animate-track",
        animatedSource,
        isRecording = isRecording,
        showAprilFoolsPattern = showAprilFoolsPattern,
    )
    TracksLayer(
        "track",
        trackSource,
        isRecording = isRecording,
        showAprilFoolsPattern = showAprilFoolsPattern,
    )
    TracksLayer(
        "old-track",
        oldTrackSource,
        opacity = 0.2f,
        showAprilFoolsPattern = showAprilFoolsPattern,
    )
}

private const val EMPTY_TRACK_JSON = """{"type":"FeatureCollection","features":[]}"""
private val EMPTY_TRACK_DATA = GeoJsonData.JsonString(EMPTY_TRACK_JSON)
private const val ANIMATED_TRACK_SOURCE_ID = "animate-track-source"
private const val TRACK_SOURCE_ID = "track-source"
private const val OLD_TRACK_SOURCE_ID = "old-track-source"
private val ACTIVE_TRACK_LAYER_IDS = listOf("animate-track", "track")

@Composable
@MaplibreComposable
private fun TracksLayer(
    id: String,
    source: Source,
    isRecording: Boolean = false,
    opacity: Float = 0.6f,
    showAprilFoolsPattern: Boolean,
) {
    if (showAprilFoolsPattern) {
        TracksLayerApril1st(id, source, isRecording, opacity)
    } else {
        TracksLayerDefault(id, source, opacity)
    }
}

@Composable
@MaplibreComposable
private fun TracksLayerApril1st(
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
private fun TracksLayerDefault(
    id: String,
    source: Source,
    opacity: Float,
) {
    LineLayer(
        id = id,
        source = source,
        opacity = const(opacity),
        cap = const(LineCap.Round),
        dasharray = const(listOf(0f, 2f)),
        width = const(6.dp),
        color = const(Color.Location),
    )
}
