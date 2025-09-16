package de.westnordost.streetcomplete.ui.common.item_select

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.unit.dp

/** Just an image with a description text to the right, centered vertically */
@Composable
fun ImageWithDescription(
    painter: Painter?,
    title: String?,
    description: String?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (painter != null) {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.clip(MaterialTheme.shapes.medium)
            )
        }
        Column(modifier = Modifier.weight(1f).padding(4.dp)) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.subtitle2,
                )
            }
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.caption.copy(hyphens = Hyphens.Auto),
                )
            }
        }
    }
}
