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
import de.westnordost.streetcomplete.ui.theme.Dimensions
import de.westnordost.streetcomplete.ui.theme.divider

/** Surface in the shape of a speech bubble with a default border stroke and inner padding. */
@Composable
fun SpeechBubble(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = Dimensions.speechBubbleCornerRadius,
    arrowSize: Dp = cornerRadius * 0.75f,
    arrowDirection: SpeechBubbleArrowDirection = SpeechBubbleArrowDirection.Bottom,
    arrowPlacementBias: Float = 0f,
    color: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(color),
    border: BorderStroke? = BorderStroke(1.dp, MaterialTheme.colors.divider),
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
