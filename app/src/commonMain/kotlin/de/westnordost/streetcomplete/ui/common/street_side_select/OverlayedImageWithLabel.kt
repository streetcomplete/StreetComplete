package de.westnordost.streetcomplete.ui.common.street_side_select

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.common.TextWithHalo

/** Like ImageWithLabel, but using TextWithHalo for the text */
@Composable
fun OverlayedImageWithLabel(
    modifier: Modifier = Modifier,
    image: Painter?,
    label: String?,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        if (image != null) {
            Image(image, label)
        }
        if (label != null) {
            TextWithHalo(
                text = label,
                style = MaterialTheme.typography.caption.copy(hyphens = Hyphens.Auto),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(2.dp)
            )
        }
    }
}
