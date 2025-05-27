package de.westnordost.streetcomplete.screens.user.achievements

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import de.westnordost.streetcomplete.R

@Composable
fun AnimatedTadaShine(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition("ta-da shine rotation")
    val rotation by infiniteTransition.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(15000, 0, LinearEasing)),
        "ta-da shine rotation"
    )

    TadaShine(modifier.rotate(rotation * 2f))
    TadaShine(modifier.rotate(180f - rotation))
}

@Composable
private fun TadaShine(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.shine),
        contentDescription = null,
        modifier = modifier
            .fillMaxSize()
            .scale(3.0f),
        alignment = Alignment.Center,
        contentScale = ContentScale.Crop,
    )
}

@Preview
@Composable
private fun PreviewAnimatedTadaShine() {
    AnimatedTadaShine()
}
