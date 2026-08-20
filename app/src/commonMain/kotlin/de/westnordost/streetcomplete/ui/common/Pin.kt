package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.screens.main.map.PinPainter
import org.jetbrains.compose.resources.painterResource

/** A pin with an image on the pin head, usually a quest icon. The point the pin is pointing at is
 *  exactly the center of this composable. */
@Composable
fun Pin(
    iconPainter: Painter,
    modifier: Modifier = Modifier
) {
    Image(
        painter = PinPainter(
            iconPainter = iconPainter,
            pinPainter = painterResource(Res.drawable.pin),
            pinShadowPainter = painterResource(Res.drawable.pin_shadow)
        ),
        contentDescription = null,
        modifier = modifier
            .padding(end = 10.dp, bottom = 71.dp)
            .size(71.dp)
    )
}

@Composable
@Preview
private fun PinPreview() {
    Pin(painterResource(Res.drawable.quest_recycling))
}
