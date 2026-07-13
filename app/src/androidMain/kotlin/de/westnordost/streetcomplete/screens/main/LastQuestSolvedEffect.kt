package de.westnordost.streetcomplete.screens.main

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.ktx.toPx
import de.westnordost.streetcomplete.util.sound.SoundEffectPlayer
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.random.Random

@Composable
fun LastQuestSolvedEffect(
    questSolvedEvent: QuestSolvedEvent,
    soundFx: SoundEffectPlayer = koinInject()
) {
    val scale = remember(questSolvedEvent) { Animatable(0f) }
    val fling = remember(questSolvedEvent) { Animatable(0f) }
    val iconSize = 44.dp.toPx()
    var parentWidth by remember { mutableIntStateOf(0) }
    val targetX = when (LocalLayoutDirection.current) {
        LayoutDirection.Ltr -> 0f
        LayoutDirection.Rtl -> parentWidth - iconSize
    }

    LaunchedEffect(questSolvedEvent) {
        soundFx.play("plop${Random.nextInt(4)}.wav")

        scale.animateTo(2.0f, animationSpec = SpringSpec(Spring.DampingRatioMediumBouncy))
        launch { scale.animateTo(0.4f) }
        launch { fling.animateTo(1f) }
    }

    Box(Modifier
        .safeDrawingPadding()
        .fillMaxSize()
        .onSizeChanged { parentWidth = it.width }
    ) {
        if (fling.value < 1f) {
            Box(Modifier
                .align(AbsoluteAlignment.TopLeft)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    alpha = 1f - fling.value * 0.67f
                    translationX = (questSolvedEvent.position.x - iconSize/2) * (1f - fling.value) + targetX * fling.value
                    translationY = (questSolvedEvent.position.y - iconSize/2) * (1f - fling.value)
                }
                .background(Color.White, CircleShape)
            ) {
                Image(
                    painter = painterResource(questSolvedEvent.iconResId),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(2.dp)
                        .size(42.dp)
                        .shadow(12.dp, CircleShape)
                )
            }
        }
    }
}

data class QuestSolvedEvent(val iconResId: Int, val position: Offset)
