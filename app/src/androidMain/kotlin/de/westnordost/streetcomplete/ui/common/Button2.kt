package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ButtonElevation
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape

enum class ButtonStyle { Default, Outlined, Text, }

/** Same as [androidx.compose.material.Button] only that the style can be set with the [style]
 *  parameter rather than having to use a different composable. */
@Composable
fun Button2(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ButtonStyle = ButtonStyle.Default,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    elevation: ButtonElevation? = style.elevation,
    shape: Shape = style.shape,
    border: BorderStroke? = style.border,
    colors: ButtonColors = style.buttonColors,
    contentPadding: PaddingValues = style.contentPadding,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource,
        elevation = elevation,
        shape = shape,
        border = border,
        colors = colors,
        contentPadding = contentPadding,
        content = content,
    )
}

val ButtonStyle.elevation : ButtonElevation? @Composable
    get() = when (this) {
        ButtonStyle.Default -> ButtonDefaults.elevation()
        ButtonStyle.Outlined -> null
        ButtonStyle.Text -> null
    }

val ButtonStyle.shape : Shape @Composable @ReadOnlyComposable
    get() = when (this) {
        ButtonStyle.Default -> MaterialTheme.shapes.small
        ButtonStyle.Outlined -> MaterialTheme.shapes.small
        ButtonStyle.Text -> MaterialTheme.shapes.small
    }

val ButtonStyle.border : BorderStroke? @Composable
get() = when (this) {
        ButtonStyle.Default -> null
        ButtonStyle.Outlined -> ButtonDefaults.outlinedBorder
        ButtonStyle.Text -> null
    }

val ButtonStyle.buttonColors : ButtonColors @Composable
get() = when (this) {
        ButtonStyle.Default -> ButtonDefaults.buttonColors()
        ButtonStyle.Outlined -> ButtonDefaults.outlinedButtonColors()
        ButtonStyle.Text -> ButtonDefaults.textButtonColors()
    }

val ButtonStyle.contentPadding : PaddingValues
get() = when (this) {
        ButtonStyle.Default -> ButtonDefaults.ContentPadding
        ButtonStyle.Outlined -> ButtonDefaults.ContentPadding
        ButtonStyle.Text -> ButtonDefaults.TextButtonContentPadding
    }
