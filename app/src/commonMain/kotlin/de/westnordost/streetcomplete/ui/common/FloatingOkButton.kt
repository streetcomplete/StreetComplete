package de.westnordost.streetcomplete.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.painterResource

/** Floating OK (check) button with animated pop-in/pop-out matching the View-based animations. */
@Composable
fun FloatingOkButton(
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = scaleIn(tween(100), initialScale = 0.5f) + fadeIn(tween(100)),
        exit = scaleOut(tween(100), targetScale = 0.5f) + fadeOut(tween(100)),
    ) {
        FloatingActionButton(
            onClick = onClick,
            shape = CircleShape,
            backgroundColor = MaterialTheme.colors.secondary,
            modifier = Modifier.size(72.dp),
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_check_48dp),
                contentDescription = null,
                tint = MaterialTheme.colors.onSecondary,
            )
        }
    }
}
