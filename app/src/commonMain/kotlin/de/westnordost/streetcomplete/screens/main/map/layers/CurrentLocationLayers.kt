package de.westnordost.streetcomplete.screens.main.map.layers

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring.StiffnessLow
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.location_nyan
import de.westnordost.streetcomplete.resources.location_shadow
import de.westnordost.streetcomplete.resources.location_view_direction
import de.westnordost.streetcomplete.screens.main.map.animateLatLonAsState
import de.westnordost.streetcomplete.screens.main.map.inMeters
import de.westnordost.streetcomplete.screens.main.map.toGeometry
import de.westnordost.streetcomplete.ui.theme.Location
import de.westnordost.streetcomplete.util.ktx.isApril1st
import de.westnordost.streetcomplete.util.math.normalizeDegrees
import org.jetbrains.compose.resources.painterResource
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.value.CirclePitchAlignment
import org.maplibre.compose.expressions.value.IconPitchAlignment
import org.maplibre.compose.expressions.value.IconRotationAlignment
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.util.MaplibreComposable

/** Displays the location + direction + accuracy marker on the map. [heading] is the compass
 *  heading in degrees, clockwise from north. */
@Composable @MaplibreComposable
fun CurrentLocationLayers(
    position: LatLon,
    accuracy: Float,
    heading: Float?
) {
    // Use the same spring as TracksLayers so the marker and track endpoint move together.
    val animatedPosition by animateLatLonAsState(targetValue = position)
    val animatedAccuracy by animateFloatAsState(
        targetValue = accuracy,
        animationSpec = spring(stiffness = StiffnessLow),
    )

    // let's not check for the date on every recomposition :-)
    val isApril1st = remember { isApril1st() }

    val source = rememberGeoJsonSource(GeoJsonData.Features(animatedPosition.toGeometry()))

    CircleLayer(
        id = "accuracy",
        source = source,
        opacity = const(0.15f),
        color = const(Color.Location),
        radius = inMeters(
            width = animatedAccuracy,
            latitude = animatedPosition.latitude
        ),
        strokeOpacity = const(0.5f),
        strokeColor = const(Color.Location),
        strokeWidth = const(1.dp),
        pitchAlignment = const(CirclePitchAlignment.Map),
    )
    if (heading != null) {
        val animatedHeading by animateHeadingAsState(heading)
        SymbolLayer(
            id = "direction",
            source = source,
            iconImage = image(painterResource(Res.drawable.location_view_direction)),
            iconAllowOverlap = const(true),
            iconIgnorePlacement = const(true),
            // map-aligned: the heading is absolute and unaffected by map rotation
            iconRotate = const(animatedHeading),
            iconRotationAlignment = const(IconRotationAlignment.Map),
            iconPitchAlignment = const(IconPitchAlignment.Map),
        )
    }
    SymbolLayer(
        id = "location-shadow",
        source = source,
        iconImage = image(painterResource(Res.drawable.location_shadow)),
        iconAllowOverlap = const(true),
        iconIgnorePlacement = const(true),
        iconPitchAlignment = const(IconPitchAlignment.Map),
    )
    if (!isApril1st) {
        CircleLayer(
            id = "location",
            source = source,
            color = const(Color.Location),
            radius = const(8.dp),
            strokeColor = const(Color.White),
            strokeWidth = const(2.dp),
            pitchAlignment = const(CirclePitchAlignment.Map)
        )
    } else {
        SymbolLayer(
            id = "location-nyan",
            source = source,
            iconImage = image(painterResource(Res.drawable.location_nyan)),
            iconSize = const(2f),
            iconAllowOverlap = const(true),
            iconIgnorePlacement = const(true),
            iconPitchAlignment = const(IconPitchAlignment.Viewport),
        )
    }
}

/** Smoothly turns towards the [heading] in degrees, always along the shorter arc */
@Composable
private fun animateHeadingAsState(heading: Float): State<Float> {
    val animation = remember { Animatable(heading) }
    LaunchedEffect(heading) {
        // Choose the shorter arc from the current animated value, including when interrupted. The
        // target may lie outside 0..360, so the value is normalized before each turn to keep it
        // from drifting.
        animation.snapTo(normalizeDegrees(animation.value))
        val target = normalizeDegrees(heading, animation.value - 180f)
        animation.animateTo(target, tween(200, easing = FastOutSlowInEasing))
    }
    return animation.asState()
}
