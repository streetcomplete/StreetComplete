package de.westnordost.streetcomplete.ui.common.speech_bubble

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.theme.Dimensions
import de.westnordost.streetcomplete.ui.theme.divider

/** A spech bubble without an arrow, so basically mostly a surface with rounded corners. However,
 *  there are some common defaults, so it makes sense to put it into an own composable. */
@Composable
fun SpeechBubbleNoArrow(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = Dimensions.speechBubbleCornerRadius,
    color: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(color),
    elevation: Dp = 0.dp,
    border: BorderStroke? = BorderStroke(1.dp, MaterialTheme.colors.divider),
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = color,
        contentColor = contentColor,
        border = border,
        elevation = elevation,
    ) {
        Box(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}
