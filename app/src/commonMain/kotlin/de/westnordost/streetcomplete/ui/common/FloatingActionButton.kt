package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ButtonElevation
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/** Same as the normal FloatingActionButton, but the FloatingActionButton from Material2 doesn't
 *  have an [enabled] parameter … */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    elevation: ButtonElevation? = FloatingActionButtonDefaults.elevation(),
    shape: Shape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
    border: BorderStroke? = null,
    colors: ButtonColors = FloatingActionButtonDefaults.buttonColors(),
    content: @Composable () -> Unit,
) {
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val contentColor by colors.contentColor(enabled)
    Surface(
        onClick = onClick,
        modifier = modifier.semantics { role = Role.Button },
        enabled = enabled,
        shape = shape,
        color = colors.backgroundColor(enabled).value,
        contentColor = contentColor.copy(alpha = 1f),
        border = border,
        elevation = elevation?.elevation(enabled, interactionSource)?.value ?: 0.dp,
        interactionSource = interactionSource,
    ) {
        CompositionLocalProvider(LocalContentAlpha provides contentColor.alpha) {
            ProvideTextStyle(value = MaterialTheme.typography.button) {
                Box(
                    modifier = Modifier.defaultMinSize(minWidth = FabSize, minHeight = FabSize),
                    contentAlignment = Alignment.Center
                ) {
                    content()
                }
            }
        }
    }
}

private val FabSize = 56.dp

object FloatingActionButtonDefaults {
    @Composable fun elevation() = ButtonDefaults.elevation(
        defaultElevation = 6.dp,
        pressedElevation = 12.dp,
        disabledElevation = 6.dp,
        hoveredElevation = 8.dp,
        focusedElevation = 8.dp,
    )

    @Composable fun buttonColors() = ButtonDefaults.buttonColors(
        backgroundColor = MaterialTheme.colors.secondary
    )
}

