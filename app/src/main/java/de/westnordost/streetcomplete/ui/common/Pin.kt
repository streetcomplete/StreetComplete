package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R

@Composable
fun Pin(
    iconPainter: Painter,
    modifier: Modifier = Modifier
) {
    Box(modifier) {
        Image(
            painter = painterResource(R.drawable.pin),
            contentDescription = null,
            modifier = Modifier.size(66.dp)
        )
        Image(
            painter = iconPainter,
            contentDescription = null,
            modifier = Modifier
                .absoluteOffset(x = 20.dp, y = 7.dp)
                .size(42.dp)
        )
    }
}

@Composable
@Preview
private fun PinPreview() {
    Pin(painterResource(R.drawable.ic_quest_recycling))
}
