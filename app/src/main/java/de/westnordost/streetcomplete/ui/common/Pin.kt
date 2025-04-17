package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R

@Composable
fun Pin(
    iconPainter: Painter,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(modifier) {
            Image(
                painter = painterResource(R.drawable.pin_shadow),
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 10.dp, bottom = 71.dp)
                    .size(71.dp, 71.dp)
            )
            Image(
                painter = painterResource(R.drawable.pin),
                contentDescription = null,
                modifier = Modifier
                    .padding(top = 5.dp)
                    .align(Alignment.TopCenter)
            )
            Image(
                painter = iconPainter,
                contentDescription = null,
                modifier = Modifier
                    .padding(top = 7.dp)
                    .size(48.dp)
                    .align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
@Preview
private fun PinPreview() {
    Pin(painterResource(R.drawable.ic_quest_recycling))
}
