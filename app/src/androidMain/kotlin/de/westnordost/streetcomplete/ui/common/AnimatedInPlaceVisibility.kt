package de.westnordost.streetcomplete.ui.common

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha

@Composable
fun AnimatedInPlaceVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    animationSpec: AnimationSpec<Float> = tween(durationMillis = 400, delayMillis = 0, easing = EaseInOut),
    label: String = "AnimatedInPlaceVisibility",
    content: @Composable () -> Unit
) {
    val alpha by animateFloatAsState(
        if (visible) 1f else 0f,
        label = label,
        animationSpec = animationSpec,
    )

    Box(modifier = modifier.alpha(alpha)) {
        content.invoke()
    }
}
