package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R

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
            painter = painterResource(R.drawable.ic_compass_needle_48dp),
            contentDescription = null,
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
