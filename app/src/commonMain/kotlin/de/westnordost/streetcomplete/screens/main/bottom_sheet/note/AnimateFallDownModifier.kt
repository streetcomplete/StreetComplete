package de.westnordost.streetcomplete.screens.main.bottom_sheet.note

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/** An animation that triggers immediately once and animates this composable falling down to its
 *  actual position. */
fun Modifier.animateFallDown(
    startDelay: Duration = Duration.ZERO,
    startOffsetY: Dp = -200.dp
): Modifier = composed {
    val y = remember { Animatable(startOffsetY.value) }
    val a = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(200.milliseconds)
        launch {
            a.animateTo(
                targetValue = 1f,
                animationSpec = spring(stiffness = Spring.StiffnessMedium)
            )
        }
        launch {
            y.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = 0.4f,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    this.graphicsLayer {
        translationY = y.value.dp.toPx()
        alpha = a.value
    }
}
