package de.westnordost.streetcomplete.ui.common.button_group

import androidx.compose.foundation.BorderStroke
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import de.westnordost.streetcomplete.ui.common.ButtonStyle
import de.westnordost.streetcomplete.ui.common.border
import de.westnordost.streetcomplete.ui.common.shape

/** A group of buttons appearing as one button in several segments. I.e. the buttons are glued
 *  together, so to speak.
 *  Somewhat similar to [segmented buttons](https://developer.android.com/develop/ui/compose/components/segmented-button)
 *  in Material3 or [segmented controls](https://developer.apple.com/design/human-interface-guidelines/segmented-controls)
 *  in Apple design but not strictly for single or multi-selection but to group related buttons
 *  generally, like Apple design's [stepper](https://developer.apple.com/design/human-interface-guidelines/steppers)
 *  or undo/redo as seen [in the illustration here](https://developer.apple.com/design/human-interface-guidelines/buttons).
 *
 *  Use [ButtonGroupButton] for its children.
 */
@Composable
fun ButtonGroup(
    modifier: Modifier = Modifier,
    style: ButtonStyle = ButtonStyle.Default,
    shape: Shape = style.shape,
    border: BorderStroke? = style.border,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        border = border,
        content = content,
    )
}

