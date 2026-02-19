package de.westnordost.streetcomplete.screens.main.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.text.TextStyle
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
 * ```
 * ▉▉▉▉▉ ▉▉▉▉ ▉▉ ▉▉▉▉ ▉▉ ▉▉▉
 * ▉▉▉ ▉▉▉▉▉▉▉▉ ▉▉ ▉▉ ▉▉▉▉▉
 * ▉▉ ▉▉▉▉▉▉▉ ▉▉
 * ▉▉▉▉▉ ▉▉ ▉▉ ▉▉▉▉ ▉▉ ▉▉▉▉▉
 * ▉▉▉ ▉▉ ▉▉▉▉ ▉▉▉ ▉▉▉▉
 * ```
 * */
@Composable
fun LoremIpsumLines(
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
) {
    val random = remember { Random(Random.Default.nextInt()) }
    val color =
        if (textStyle.color.isSpecified) {
            textStyle.color
        } else {
            LocalContentColor.current.copy(LocalContentAlpha.current)
        }
    val lineHeight = textStyle.lineHeight.toDp().toPx()
    val fontHeight = textStyle.fontSize.toDp().toPx()
    val fontWidth = fontHeight * 0.67f // characters are usually higher than wide

    Box(modifier.drawBehind {
        var y = 0f
        var x = 0f
        var w = 0
        while (y + lineHeight < size.height) { // the line must fit in full
            while (x + fontWidth * 2 < size.width) { // a two-letter word must fit
                val length = random.nextInt(2, 10) * fontWidth
                val yCenter = y + lineHeight / 2f
                val xEnd = min(x + length, size.width) // don't draw out of bounds
                drawLine(
                    color = color,
                    start = Offset(x, yCenter),
                    end = Offset(xEnd, yCenter),
                    strokeWidth = fontHeight
                )
                w++
                // random paragraphs
                if (w > random.nextInt(10, 40)) {
                    w = 0
                    break
                }
                x += length + fontWidth // add space
            }
            y += lineHeight
            x = 0f
        }
    })
}
