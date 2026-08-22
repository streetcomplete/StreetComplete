package de.westnordost.streetcomplete.screens.main.map.layers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.charleskorn.kaml.YamlPathSegment.Root.location
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.screens.main.map.animateLatLonAsState
import de.westnordost.streetcomplete.screens.main.map.toLineGeometry
import de.westnordost.streetcomplete.screens.main.map.toMultiLineGeometry
import de.westnordost.streetcomplete.ui.theme.Location
import de.westnordost.streetcomplete.ui.theme.Recording
import de.westnordost.streetcomplete.util.ktx.isApril1st
import org.maplibre.spatialk.geojson.MultiLineString
import org.jetbrains.compose.resources.painterResource
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.convertToBoolean
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.value.LineCap
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.Source
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.util.MaplibreComposable
import org.maplibre.spatialk.geojson.GeometryCollection

@MaplibreComposable @Composable
fun TracksLayers(
    trackpoints: List<LatLon>,
    isRecording: Boolean,
    oldTrackpointsLists: List<List<LatLon>>,
) {
    val trackLastSegment = remember(trackpoints) {
        if (trackpoints.size >= 2) trackpoints.takeLast(2) else null
    }
    val trackWithoutLast = remember(trackpoints) {
        if (trackpoints.size > 1) trackpoints.take(trackpoints.size - 1) else emptyList()
    }

    val tracksSource = rememberGeoJsonSource(
        data = GeoJsonData.Features(trackWithoutLast.toLineGeometry() ?: GeometryCollection(emptyList()))
    )
    // we want to animate the drawing of the track from the last position to the current position
    // while the position marker animates at the same time from the last position to the current
    // position (see CurrentLocationLayers)
    val animatedTracksSource = rememberGeoJsonSource(
        data = GeoJsonData.Features(
            trackLastSegment?.let {
                val animatedLastPosition by animateLatLonAsState(targetValue = it.last())
                listOf(it.first(), animatedLastPosition).toLineGeometry()
            } ?: GeometryCollection(emptyList())
        )
    )

    // old tracks are expected to not update so often
    val oldTracksSource = rememberGeoJsonSource(
        data = GeoJsonData.Features(oldTrackpointsLists.toMultiLineGeometry())
    )

    // old tracks are drawn with less alpha so the map stays well visible
    TracksLayer(
        id = "old-track",
        source = oldTracksSource,
        opacity = 0.2f
    )

    TracksLayer(
        id = "track",
        source = tracksSource,
        isRecording = isRecording,
    )

    TracksLayer(
        id = "animate-track",
        source = animatedTracksSource,
        isRecording = isRecording
    )
}

/** Displays a path(s) walked on the map */
@MaplibreComposable @Composable
private fun TracksLayer(
    id: String,
    source: Source,
    isRecording: Boolean = false,
    opacity: Float = 0.6f,
) {
    // let's not check for the date on every recomposition :-)
    val isApril1st = remember { isApril1st() }
    if (isApril1st) {
        TracksLayerApril1st(id, source, isRecording, opacity)
    } else {
        TracksLayerDefault(id, source, isRecording, opacity)
    }
}

@MaplibreComposable @Composable
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
        width = const(26.dp),
        pattern = image(painterResource(
            if (isRecording) Res.drawable.map_track_nyan_record
            else Res.drawable.map_track_nyan
        )),
    )
}

@MaplibreComposable @Composable
private fun TracksLayerDefault(
    id: String,
    source: Source,
    isRecording: Boolean,
    opacity: Float,
) {
    val recording = feature["recording"].convertToBoolean()

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
