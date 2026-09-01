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
import de.westnordost.streetcomplete.util.math.normalizeDegrees
import de.westnordost.streetcomplete.util.math.normalizeLongitude
import kotlin.math.PI
import kotlin.math.cos

internal const val LOCATION_ANIMATION_DURATION_MILLIS = 600
private const val ROTATION_ANIMATION_DURATION_MILLIS = 200

internal val AccelerateDecelerateEasing = Easing { fraction ->
    (cos((fraction + 1) * PI) / 2.0 + 0.5).toFloat()
}

/** Animates map bearing along its shortest turn, matching the legacy 200ms motion. */
@Composable
fun animateMapRotationAsState(targetValue: Float?): State<Float?> {
    val animatedValue = remember { mutableStateOf(targetValue) }
    LaunchedEffect(targetValue) {
        val startValue = animatedValue.value
        if (startValue == null || targetValue == null) {
            animatedValue.value = targetValue
            return@LaunchedEffect
        }

        Animatable(startValue).animateTo(
            targetValue = shortestRotationTarget(startValue, targetValue),
            animationSpec = tween(
                durationMillis = ROTATION_ANIMATION_DURATION_MILLIS,
                easing = AccelerateDecelerateEasing,
            ),
        ) {
            animatedValue.value = value
        }
    }
    return animatedValue
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

internal fun shortestRotationTarget(start: Float, target: Float): Float =
    normalizeDegrees(target, start - 180f)
