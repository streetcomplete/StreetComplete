package de.westnordost.streetcomplete.screens.main.map.layers

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import de.westnordost.streetcomplete.screens.main.map.AccelerateDecelerateEasing
import de.westnordost.streetcomplete.screens.main.map.LOCATION_ANIMATION_DURATION_MILLIS
import de.westnordost.streetcomplete.screens.main.map.animateLatLonAsState
import de.westnordost.streetcomplete.screens.main.map.animateMapRotationAsState
import de.westnordost.streetcomplete.screens.main.map.inMeters
import de.westnordost.streetcomplete.screens.main.map.toPosition
import de.westnordost.streetcomplete.util.ktx.isApril1st
import org.jetbrains.compose.resources.painterResource
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.value.CirclePitchAlignment
import org.maplibre.compose.expressions.value.IconPitchAlignment
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.util.MaplibreComposable
import org.maplibre.spatialk.geojson.Point

private val LocationColor = Color(0xff536dfe)
private val LocationDirectionSize = DpSize(96.dp, 96.dp)
private val LocationShadowSize = DpSize(28.dp, 28.dp)
private val LocationNyanSize = DpSize(34.dp, 22.dp)

/** Displays the current location's accuracy, bearing, shadow, and position marker. */
@Composable
@MaplibreComposable
fun CurrentLocationLayers(
    location: Location,
    rotation: Float?,
) {
    val animatedPosition by animateLatLonAsState(location.position)
    val animatedAccuracy by animateFloatAsState(
        targetValue = location.accuracy,
        animationSpec = tween(
            durationMillis = LOCATION_ANIMATION_DURATION_MILLIS,
            easing = AccelerateDecelerateEasing,
        ),
        label = "LocationAccuracy",
    )
    val animatedRotation by animateMapRotationAsState(rotation)
    val aprilFirst = remember { isApril1st() }

    // TODO(maplibre-compose): Restore the legacy source's volatile flag when GeoJsonOptions exposes it.
    val source = rememberGeoJsonSource(
        data = GeoJsonData.Features(Point(animatedPosition.toPosition()))
    )

    CircleLayer(
        id = "accuracy",
        source = source,
        opacity = const(0.15f),
        color = const(LocationColor),
        radius = inMeters(animatedAccuracy, animatedPosition.latitude),
        strokeOpacity = const(0.5f),
        strokeColor = const(LocationColor),
        strokeWidth = const(1.dp),
        pitchAlignment = const(CirclePitchAlignment.Map),
    )

    // Android intentionally omits these two assets during its April 1 easter egg.
    if (!aprilFirst) {
        if (animatedRotation != null) {
            SymbolLayer(
                id = "direction",
                source = source,
                iconImage = image(
                    painterResource(Res.drawable.location_view_direction),
                    size = LocationDirectionSize,
                ),
                iconAllowOverlap = const(true),
                iconIgnorePlacement = const(true),
                iconRotate = const(animatedRotation!!),
                iconPitchAlignment = const(IconPitchAlignment.Map),
            )
        }
        SymbolLayer(
            id = "location-shadow",
            source = source,
            // Android's layer-list painter reports a zero intrinsic size. MapLibre Compose
            // rasterizes style painters eagerly, so preserve the drawable's declared size here.
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
        color = const(LocationColor),
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
