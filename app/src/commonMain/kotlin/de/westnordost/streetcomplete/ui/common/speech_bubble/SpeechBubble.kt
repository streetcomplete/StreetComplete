package de.westnordost.streetcomplete.ui.common.speech_bubble

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Surface in the shape of a speech bubble with a border stroke and a default inner padding by
 *  default. */
@Composable
fun SpeechBubble(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    arrowSize: Dp = 12.dp,
    arrowDirection: SpeechBubbleArrowDirection = SpeechBubbleArrowDirection.Bottom,
    arrowPlacementBias: Float = 0f,
    color: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(color),
    border: BorderStroke? = BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.12f)),
    elevation: Dp = 0.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    content: @Composable () -> Unit,
) {
    val shape = SpeechBubbleShape(
        cornerRadius = cornerRadius,
        arrowSize = arrowSize,
        arrowDirection = arrowDirection,
        arrowPlacementBias = arrowPlacementBias,
    )
    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
        border = border,
        elevation = elevation,
    ) {
        Box(modifier = Modifier
            .clip(shape)
            .padding(shape.contentPadding)
            .padding(contentPadding)
        ) {
            content()
        }
    }
}
