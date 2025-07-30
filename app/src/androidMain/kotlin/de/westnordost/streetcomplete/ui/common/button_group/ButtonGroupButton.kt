package de.westnordost.streetcomplete.ui.common.button_group

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ButtonElevation
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.common.ButtonStyle
import de.westnordost.streetcomplete.ui.common.buttonColors
import de.westnordost.streetcomplete.ui.common.contentPadding
import de.westnordost.streetcomplete.ui.common.elevation

/** A button in a button group. See [ButtonGroup]. */
@Composable
fun ButtonGroupButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ButtonStyle = ButtonStyle.Default,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    elevation: ButtonElevation? = style.elevation,
    colors: ButtonColors = style.buttonColors,
    contentPadding: PaddingValues = style.contentPadding,
    content: @Composable RowScope.() -> Unit
) {
    // Implementation is pretty much copied from Button, only that shape and border is not applied
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val contentColor by colors.contentColor(enabled)
    Surface(
        modifier = modifier
            .semantics { role = Role.Button }
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                enabled = enabled,
                onClick = onClick
            ),
        color = colors.backgroundColor(enabled).value,
        contentColor = contentColor.copy(alpha = 1f),
        elevation = elevation?.elevation(enabled, interactionSource)?.value ?: 0.dp,
    ) {
        CompositionLocalProvider(LocalContentAlpha provides contentColor.alpha) {
            ProvideTextStyle(value = MaterialTheme.typography.button) {
                Row(
                    modifier = Modifier
                        .defaultMinSize(
                            minWidth = ButtonDefaults.MinWidth,
                            minHeight = ButtonDefaults.MinHeight
                        )
                        .padding(contentPadding),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    content = content
                )
            }
        }
    }
}
