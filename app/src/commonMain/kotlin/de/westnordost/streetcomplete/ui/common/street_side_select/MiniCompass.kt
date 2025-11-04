package de.westnordost.streetcomplete.ui.common.street_side_select

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.compass
import de.westnordost.streetcomplete.resources.compass_needle_48
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Small compass that resembles somewhat a smaller version of the CompassButton but is not a button
 * */
@Composable
fun MiniCompass(
    modifier: Modifier = Modifier,
    rotation: Float = 0f,
    tilt: Float = 0f,
) {
    AnimatedVisibility(
        visible = rotation != 0f || tilt != 0f,
        modifier = modifier,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Image(
            painter = painterResource(Res.drawable.compass_needle_48),
            contentDescription = stringResource(Res.string.compass),
            modifier = Modifier
                .border(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.12f), CircleShape)
                .background(MaterialTheme.colors.surface, CircleShape)
                .padding(4.dp)
                .size(24.dp)
                .graphicsLayer(
                    rotationZ = rotation,
                    rotationX = tilt
                )
        )
    }
}
