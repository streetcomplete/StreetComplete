package de.westnordost.streetcomplete.ui.util

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith

val FallDownTransitionSpec: AnimatedContentTransitionScope<*>.() -> ContentTransform = {
    (fadeIn(tween(220)) + scaleIn(tween(220), initialScale = 2f))
        .togetherWith(fadeOut(tween(90)))
}
