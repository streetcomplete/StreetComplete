package de.westnordost.streetcomplete.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Floating OK (check) button with animated pop-in/pop-out*/
@Composable
fun FloatingOkButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    enabled: Boolean = true,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = scaleIn(tween(100), initialScale = 0.5f) + fadeIn(tween(100)),
        exit = scaleOut(tween(100), targetScale = 0.5f) + fadeOut(tween(100)),
    ) {
        FloatingActionButton(
            onClick = onClick,
            enabled = enabled
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_check_32),
                contentDescription = stringResource(Res.string.ok),
            )
        }
    }
}
