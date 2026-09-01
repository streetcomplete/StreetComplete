package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.math.normalizeLongitude
import kotlin.math.PI
import kotlin.math.cos

private const val LOCATION_ANIMATION_DURATION_MILLIS = 600

private val AccelerateDecelerateEasing = Easing { fraction ->
    (cos((fraction + 1) * PI) / 2.0 + 0.5).toFloat()
}

/** Animates along the shortest path across the antimeridian, matching the legacy 600ms motion. */
@Composable
fun animateLatLonAsState(
    targetValue: LatLon,
    initialValue: LatLon = targetValue,
): State<LatLon> {
    val animatedValue = remember { mutableStateOf(initialValue) }
    LaunchedEffect(targetValue) {
        val startValue = animatedValue.value
        Animatable(0f).animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = LOCATION_ANIMATION_DURATION_MILLIS,
                easing = AccelerateDecelerateEasing,
            )
        ) {
            animatedValue.value = interpolateLatLon(startValue, targetValue, value.toDouble())
        }
    }
    return animatedValue
}

internal fun interpolateLatLon(start: LatLon, end: LatLon, fraction: Double): LatLon {
    val longitudeDelta = normalizeLongitude(end.longitude - start.longitude)
    return LatLon(
        latitude = start.latitude + (end.latitude - start.latitude) * fraction,
        longitude = normalizeLongitude(start.longitude + longitudeDelta * fraction),
    )
}
