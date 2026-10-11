package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.animation.core.Spring.StiffnessLow
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.math.normalizeLongitude

/** Animates a LatLon to a [targetValue] position. Also works when crossing the antimeridian. */
@Composable
fun animateLatLonAsState(
    targetValue: LatLon,
    animationSpec: SpringSpec<LatLon> = spring(stiffness = StiffnessLow),
    label: String = "LatLonAnimation"
): State<LatLon> {
    val origin = remember { targetValue }
    var targetLongitude by remember { mutableStateOf(targetValue.longitude) }

    LaunchedEffect(targetValue.longitude) {
        targetLongitude += normalizeLongitude(targetValue.longitude - targetLongitude)
    }

    // Animate local deltas so Float animation vectors retain precision for small GPS movements.
    val animatedOffset by animateOffsetAsState(
        targetValue = Offset(
            (targetValue.latitude - origin.latitude).toFloat(),
            (targetLongitude - origin.longitude).toFloat(),
        ),
        animationSpec = spring(
            dampingRatio = animationSpec.dampingRatio,
            stiffness = animationSpec.stiffness,
            visibilityThreshold = Offset(
                (animationSpec.visibilityThreshold?.latitude ?: 1e-7).toFloat(),
                (animationSpec.visibilityThreshold?.longitude ?: 1e-7).toFloat(),
            ),
        ),
        label = label,
    )

    return remember { derivedStateOf { LatLon(
        latitude = origin.latitude + animatedOffset.x,
        longitude = normalizeLongitude(origin.longitude + animatedOffset.y),
    ) } }
}
