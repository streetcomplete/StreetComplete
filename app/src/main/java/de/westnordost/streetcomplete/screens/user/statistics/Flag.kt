package de.westnordost.streetcomplete.screens.user.statistics

import androidx.compose.foundation.Image
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun Flag(
    countryCode: String,
    modifier: Modifier = Modifier,
) {
    val painter = flagPainterResource(countryCode)
    if (painter != null) {
        val stroke = 1.dp
        val color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
        Image(
            painter = painter,
            contentDescription = countryCode,
            modifier = modifier.drawWithContent {
                drawContent()
                // add stroke so that white flags are visible
                inset((stroke.toPx()/2).coerceAtLeast(1f)) {
                    drawRect(color, style = Stroke(stroke.toPx()))
                }
            })
    }
}

@Composable
fun flagPainterResource(countryCode: String): Painter? {
    val context = LocalContext.current
    val lowerCaseCountryCode = countryCode.lowercase().replace('-', '_')
    val id = context.resources.getIdentifier("ic_flag_$lowerCaseCountryCode", "drawable", context.packageName)
    return if (id != 0) painterResource(id) else null
}
