package de.westnordost.streetcomplete.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** An animated visibility for something that should appear as a full-screen dialog */
@Composable
fun AnimatedScreenVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    label: String = "AnimatedScreenVisibility",
    content: @Composable() AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 10 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 10 }),
        modifier = modifier,
        label = label,
        content = content
    )
}
