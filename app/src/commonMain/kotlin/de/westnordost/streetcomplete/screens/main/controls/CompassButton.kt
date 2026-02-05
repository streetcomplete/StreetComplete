package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.compass
import de.westnordost.streetcomplete.resources.compass_needle_48
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

/** Map button showing current compass orientation in relation to the map. Invisible if north-up and
 *  no tilt */
@Composable
fun CompassButton(
    onClick: () -> Unit,
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
        MapButton(
            onClick = onClick,
            contentPadding = 8.dp
        ) {
            Image(
                painter = painterResource(Res.drawable.compass_needle_48),
                contentDescription = stringResource(Res.string.compass),
                modifier = Modifier
                    .size(32.dp)
                    .graphicsLayer(
                        rotationZ = rotation,
                        rotationX = tilt
                    )
            )
        }
    }
}

@Preview
@Composable
private fun PreviewCompassButton() {
    CompassButton(
        onClick = {},
        rotation = 30f,
        tilt = 0f
    )
}
