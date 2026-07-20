package de.westnordost.streetcomplete.ui.util

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset

val FallDownTransitionSpec: AnimatedContentTransitionScope<*>.() -> ContentTransform = {
    (fadeIn(tween(220)) + scaleIn(tween(220), initialScale = 2f))
        .togetherWith(fadeOut(tween(90)))
}

val SlideStartHorizontally: AnimatedContentTransitionScope<*>.() -> ContentTransform = {
    val spring = spring(
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = IntOffset.VisibilityThreshold
    )
    slideIntoContainer(SlideDirection.Start, spring).togetherWith(
        slideOutOfContainer(SlideDirection.Start, spring)
    )
}
