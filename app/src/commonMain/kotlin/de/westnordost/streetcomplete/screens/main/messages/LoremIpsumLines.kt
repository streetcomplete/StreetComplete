package de.westnordost.streetcomplete.screens.main.messages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.ktx.toDp
import de.westnordost.streetcomplete.ui.ktx.toPx
import kotlin.math.min
import kotlin.random.Random

/**
 * Paints something like this
 *
 * ▉▉▉▉▉ ▉▉▉▉ ▉▉ ▉▉▉▉ ▉▉ ▉▉▉
 * ▉▉▉ ▉▉▉▉▉▉▉▉ ▉▉ ▉▉ ▉▉▉▉▉
 * ▉▉ ▉▉▉▉▉▉▉ ▉▉
 * ▉▉▉▉▉ ▉▉ ▉▉ ▉▉▉▉ ▉▉ ▉▉▉▉▉
 * ▉▉▉ ▉▉ ▉▉▉▉ ▉▉▉ ▉▉▉▉
 * */
@Composable
fun LoremIpsumLines(
    modifier: Modifier = Modifier,
    color: Color = LocalTextStyle.current.color,
    fontSize: TextUnit = LocalTextStyle.current.fontSize
) {
    val random = remember { Random(Random.Default.nextInt()) }
    val lineHeight = fontSize.toDp().toPx()
    val fontHeight = lineHeight * 0.67f // characters usually don't fill the whole line
    val fontWidth = fontHeight * 0.67f // characters are usually higher than wide

    Box(modifier.drawBehind {
        var y = 0f
        var x = 0f
        var w = 0
        while (y + lineHeight < size.height) { // the line must fit in full
            while (x + fontWidth * 2 < size.width) { // a two-letter word must fit
                val length = random.nextInt(2, 10) * fontWidth
                val yCenter = y + lineHeight - fontHeight / 2f
                val xEnd = min(x + length, size.width) // don't draw out of bounds
                drawLine(
                    color = color,
                    start = Offset(x, yCenter),
                    end = Offset(xEnd, yCenter),
                    strokeWidth = fontHeight
                )
                w++
                // random paragraphs
                if (w > random.nextInt(10, 50)) {
                    break
                }
                x += length + fontWidth // add space
            }
            y += lineHeight
            x = 0f
        }
    })
}

@Preview
@Composable
private fun LoremIpsumLinesPreview() {
    LoremIpsumLines(Modifier.width(240.dp).height(600.dp))
}
