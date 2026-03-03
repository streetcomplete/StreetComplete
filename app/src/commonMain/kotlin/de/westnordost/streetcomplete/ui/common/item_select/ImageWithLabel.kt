package de.westnordost.streetcomplete.ui.common.item_select

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.ktx.conditional

/** Just an image with a label below, centered horizontally. The image can be rotated. */
@Composable
fun ImageWithLabel(
    painter: Painter?,
    label: String?,
    modifier: Modifier = Modifier,
    imageRotation: Float? = null,
    imageSize: DpSize? = null,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        if (painter != null) {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .conditional(imageSize != null) { size(imageSize!!) }
                    .conditional(imageRotation != null) { rotate(imageRotation!!) },
            )
        }
        if (label != null) {
            Text(
                text = label,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.caption.copy(hyphens = Hyphens.Auto),
                modifier = Modifier.padding(2.dp)
            )
        }
    }
}
