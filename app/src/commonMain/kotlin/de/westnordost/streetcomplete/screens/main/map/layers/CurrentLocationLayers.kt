package de.westnordost.streetcomplete.screens.main.map.layers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.location.Location
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.location_nyan
import de.westnordost.streetcomplete.resources.location_shadow
import de.westnordost.streetcomplete.resources.location_view_direction
import de.westnordost.streetcomplete.screens.main.map.animateMapRotationAsState
import de.westnordost.streetcomplete.screens.main.map.animateNullableFloatAsState
import de.westnordost.streetcomplete.screens.main.map.animateNullableLatLonAsState
import de.westnordost.streetcomplete.screens.main.map.inMeters
import de.westnordost.streetcomplete.screens.main.map.toPosition
import de.westnordost.streetcomplete.ui.theme.Location
import de.westnordost.streetcomplete.util.ktx.isApril1st
import kotlin.time.Duration
import kotlinx.serialization.json.JsonObject
import org.jetbrains.compose.resources.painterResource
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.value.CirclePitchAlignment
import org.maplibre.compose.expressions.value.IconPitchAlignment
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.TransitionOptions
import org.maplibre.compose.util.MaplibreComposable
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.Point

private val LocationDirectionSize = DpSize(96.dp, 96.dp)
private val LocationShadowSize = DpSize(28.dp, 28.dp)
private val LocationNyanSize = DpSize(34.dp, 22.dp)

/** Displays the current location's accuracy, bearing, shadow, and position marker. */
@Composable
@MaplibreComposable
fun CurrentLocationLayers(
    location: Location?,
    rotation: Float?,
) {
    val animatedPosition by animateNullableLatLonAsState(location?.position)
    val animatedAccuracy by animateNullableFloatAsState(location?.accuracy)
    val animatedRotation by animateMapRotationAsState(rotation.takeIf { location != null })
    val aprilFirst = remember { isApril1st() }

    val data = animatedPosition?.let { position ->
        GeoJsonData.Features(Point(position.toPosition()))
    } ?: EMPTY_LOCATION_DATA
    val source = rememberGeoJsonSource(data)

    CurrentLocationStyleLayers(
        source,
        aprilFirst,
        accuracy = animatedAccuracy ?: 0f,
        latitude = animatedPosition?.latitude ?: 0.0,
        rotation = animatedRotation,
    )
}

@Composable
@MaplibreComposable
private fun CurrentLocationStyleLayers(
    source: org.maplibre.compose.sources.VectorSource,
    aprilFirst: Boolean,
    accuracy: Float,
    latitude: Double,
    rotation: Float?,
) {
    CircleLayer(
        id = "accuracy",
        source = source,
        opacity = const(0.15f),
        color = const(Color.Location),
        radius = inMeters(accuracy, latitude),
        radiusTransition = TransitionOptions(duration = Duration.ZERO),
        strokeOpacity = const(0.5f),
        strokeColor = const(Color.Location),
        strokeWidth = const(1.dp),
        pitchAlignment = const(CirclePitchAlignment.Map),
    )

    // Android intentionally omits these two assets during its April 1 easter egg.
    if (!aprilFirst) {
        SymbolLayer(
            id = "direction",
            source = source,
            visible = rotation != null,
            iconImage = image(
                painterResource(Res.drawable.location_view_direction),
                size = LocationDirectionSize,
            ),
            iconAllowOverlap = const(true),
            iconIgnorePlacement = const(true),
            iconRotate = const(rotation ?: 0f),
            iconPitchAlignment = const(IconPitchAlignment.Map),
        )
        SymbolLayer(
            id = "location-shadow",
            source = source,
            // Keep the intended map-image dimensions independent of painter intrinsic sizing.
            iconImage = image(
                painterResource(Res.drawable.location_shadow),
                size = LocationShadowSize,
            ),
            iconAllowOverlap = const(true),
            iconIgnorePlacement = const(true),
            iconPitchAlignment = const(IconPitchAlignment.Map),
        )
    }

    CircleLayer(
        id = "location",
        source = source,
        color = const(Color.Location),
        radius = const(8.dp),
        strokeColor = const(Color.White),
        strokeWidth = const(2.dp),
        pitchAlignment = const(CirclePitchAlignment.Map),
    )

    if (aprilFirst) {
        SymbolLayer(
            id = "location-nyan",
            source = source,
            iconImage = image(
                painterResource(Res.drawable.location_nyan),
                size = LocationNyanSize,
            ),
            iconSize = const(2f),
            iconAllowOverlap = const(true),
            iconIgnorePlacement = const(true),
            iconPitchAlignment = const(IconPitchAlignment.Viewport),
        )
    }
}

private val EMPTY_LOCATION_DATA = GeoJsonData.Features(
    FeatureCollection<Geometry, JsonObject>(emptyList())
)
