package de.westnordost.streetcomplete.ui.common

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import de.westnordost.streetcomplete.ui.ktx.pxToDp

/** Animate in and out child composables in-place, i.e. when they become invisible, the space they
 *  took when visible should still be reserved. However, the place is just reserved, i.e. one can
 *  not e.g. click on the invisible elements. */
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
    var width by rememberSaveable { mutableStateOf(0) }
    var height by rememberSaveable { mutableStateOf(0) }

    Box(modifier = modifier
        .alpha(alpha)
        .onSizeChanged {
            if (visible) {
                width = it.width
                height = it.height
            }
        }
    ) {
        if (alpha > 0f) {
            content()
        } else {
            Spacer(Modifier.size(width.pxToDp(), height.pxToDp()))
        }
    }
}
