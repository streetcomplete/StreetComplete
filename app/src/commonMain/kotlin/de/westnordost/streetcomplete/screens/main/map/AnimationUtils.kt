package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.runtime.State
import androidx.compose.animation.core.Spring.StiffnessLow
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.math.normalizeLongitude

/** Animates a LatLon to a [targetValue] position. Also works when crossing the antimeridian. */
@Composable
fun animateLatLonAsState(
    targetValue: LatLon,
    animationSpec: SpringSpec<LatLon> = spring(stiffness = StiffnessLow),
    label: String = "LatLonAnimation"
): State<LatLon> {
    var targetLongitude by remember { mutableStateOf(targetValue.longitude) }

    LaunchedEffect(targetValue.longitude) {
        targetLongitude += normalizeLongitude(targetValue.longitude - targetLongitude)
    }

    val intAnimationSpec = spring(
        dampingRatio = animationSpec.dampingRatio,
        stiffness = animationSpec.stiffness,
        visibilityThreshold = 1
    )

    val animatedLongitude by animateIntAsState(
        targetValue = (targetLongitude * 7).toInt(),
        animationSpec = intAnimationSpec,
        label = label+"-Lon"
    )
    val animatedLatitude by animateIntAsState(
        targetValue = (targetValue.latitude * 7).toInt(),
        animationSpec = intAnimationSpec,
        label = label+"-Lat"
    )

    return remember { derivedStateOf { LatLon(
        latitude = animatedLatitude/7.0,
        longitude = normalizeLongitude(animatedLongitude/7.0)
    ) } }
}
