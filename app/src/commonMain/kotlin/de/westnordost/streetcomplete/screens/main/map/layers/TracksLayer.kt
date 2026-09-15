package de.westnordost.streetcomplete.screens.main.map.layers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.screens.main.map.animateLatLonAsState
import de.westnordost.streetcomplete.screens.main.map.toLineGeometry
import de.westnordost.streetcomplete.screens.main.map.toMultiLineGeometry
import de.westnordost.streetcomplete.screens.main.map.toPosition
import de.westnordost.streetcomplete.ui.theme.Location
import de.westnordost.streetcomplete.ui.theme.Recording
import de.westnordost.streetcomplete.util.ktx.isApril1st
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.value.LineCap
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.VectorSource
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.util.MaplibreComposable
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.GeometryCollection
import org.maplibre.spatialk.geojson.LineString

/** Display the path(s) walked on the map.
 *
 *  The current [trackpoints] will be shown in red when the user [isRecording], otherwise blue.
 *  The last segment of [trackpoints] is animated from the second last to the last position, which
 *  is done in-sync with the moving of the location puck.
 *
 *  Since the list of trackpoints is changed every time the location puck moves to a new location,
 *  for performance reasons, when the list of trackpoints gets too long, the last X trackpoints
 *  can be cut off (except if the user [isRecording]) and added to [oldTrackpointsLists]. These are
 *  displayed with less opacity and need to be updated less often. */
@MaplibreComposable @Composable
fun TracksLayers(
    trackpoints: List<LatLon>,
    isRecording: Boolean,
    oldTrackpointsLists: List<List<LatLon>>,
) {
    // let's not check for the date on every recomposition :-)
    val isApril1st = remember { isApril1st() }

    val trackLastSegment = remember(trackpoints) {
        if (trackpoints.size >= 2) trackpoints.takeLast(2) else null
    }
    val trackWithoutLast = remember(trackpoints) {
        if (trackpoints.size > 1) trackpoints.take(trackpoints.size - 1) else emptyList()
    }

    val trackData by produceState<Geometry>(EMPTY_GEOMETRY, trackWithoutLast) {
        value = withContext(Dispatchers.Default) {
            trackWithoutLast.toLineGeometry() ?: EMPTY_GEOMETRY
        }
    }
    val tracksSource = rememberGeoJsonSource(data = GeoJsonData.Features(trackData))

    // we want to animate the drawing of the track from the last position to the current position
    // while the position marker animates at the same time from the last position to the current
    // position (see CurrentLocationLayers)
    val animatedData =
        if (trackLastSegment != null) {
            val animatedLastPosition by animateLatLonAsState(targetValue = trackLastSegment.last())
            LineString(trackLastSegment.first().toPosition(), animatedLastPosition.toPosition())
        } else {
            EMPTY_GEOMETRY
        }
    val animatedTracksSource = rememberGeoJsonSource(data = GeoJsonData.Features(animatedData))

    // old tracks are expected to not update so often
    val oldTrackData by produceState<Geometry>(EMPTY_GEOMETRY, oldTrackpointsLists) {
        value = withContext(Dispatchers.Default) { oldTrackpointsLists.toMultiLineGeometry() }
    }
    val oldTracksSource = rememberGeoJsonSource(data = GeoJsonData.Features(oldTrackData))

    // old tracks are drawn with less alpha so the map stays well visible
    TracksLayer(
        id = "old-track",
        source = oldTracksSource,
        opacity = 0.2f,
        isApril1st = isApril1st,
    )

    TracksLayer(
        id = "track",
        source = tracksSource,
        isRecording = isRecording,
        isApril1st = isApril1st,
    )

    TracksLayer(
        id = "animate-track",
        source = animatedTracksSource,
        isRecording = isRecording,
        isApril1st = isApril1st,
    )
}

/** Displays a path(s) walked on the map */
@MaplibreComposable @Composable
private fun TracksLayer(
    id: String,
    source: VectorSource,
    isRecording: Boolean = false,
    opacity: Float = 0.6f,
    isApril1st: Boolean = false,
) {
    if (isApril1st) {
        TracksLayerApril1st(id, source, isRecording, opacity)
    } else {
        TracksLayerDefault(id, source, isRecording, opacity)
    }
}

@MaplibreComposable @Composable
private fun TracksLayerApril1st(
    id: String,
    source: VectorSource,
    isRecording: Boolean,
    opacity: Float,
) {
    LineLayer(
        id = id,
        source = source,
        opacity = const(opacity),
        width = const(26.dp),
        pattern = image(painterResource(
            if (isRecording) Res.drawable.track_nyan_record
            else Res.drawable.track_nyan
        )),
    )
}

@MaplibreComposable @Composable
private fun TracksLayerDefault(
    id: String,
    source: VectorSource,
    isRecording: Boolean,
    opacity: Float,
) {
    LineLayer(
        id = id,
        source = source,
        opacity = const(opacity),
        cap = const(LineCap.Round),
        dasharray = const(listOf(0, 2)),
        width = const(6.dp),
        color = const(if (isRecording) Color.Recording else Color.Location),
    )
}

private val EMPTY_GEOMETRY: Geometry = GeometryCollection(emptyList())
