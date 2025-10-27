package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.foundation.BorderStroke
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
import org.jetbrains.compose.ui.tooling.preview.Preview

/** Map button showing current compass orientation in relation to the map */
@Composable
fun CompassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    rotation: Float = 0f,
    tilt: Float = 0f,
) {
    MapButton(
        onClick = onClick,
        modifier = modifier,
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

@Preview
@Composable
private fun PreviewCompassButton() {
    CompassButton(
        onClick = {},
        rotation = 30f,
        tilt = 0f
    )
}
