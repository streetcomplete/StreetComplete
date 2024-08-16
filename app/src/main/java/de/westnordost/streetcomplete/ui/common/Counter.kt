package de.westnordost.streetcomplete.ui.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/** A text that animates changes to the number displayed like a mechanical  tally counter
 *  (https://en.wikipedia.org/wiki/Tally_counter).
 *  If [clip] is true, the fading numbers displayed above and below the actual count during
 *  animation are clipped (like they would be with a mechanical tally counter). */
@Composable
fun Counter(
    count: Int,
    modifier: Modifier = Modifier,
    clip: Boolean = false,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current
) {
    var oldCount by remember { mutableIntStateOf(count) }
    SideEffect { oldCount = count }

    val digits = count.toString().toCharArray().reversed()

    CompositionLocalProvider(LocalLayoutDirection provides LocalLayoutDirection.current.reversed()) {
        Row(modifier = modifier) {
            digits.forEachIndexed { index, digit ->
                AnimatedContent(
                    targetState = digit,
                    transitionSpec = {
                        val contentTransform = if (count > oldCount) {
                            slideInVertically { -it } + fadeIn() togetherWith slideOutVertically { it } + fadeOut()
                        } else {
                            slideInVertically { it } + fadeIn() togetherWith slideOutVertically { -it } + fadeOut()
                        }
                        contentTransform.using(SizeTransform(clip))
                    },
                    label = "CounterAnimation$index"
                ) {
                    Text(
                        text = it.toString(),
                        color = color,
                        fontSize = fontSize,
                        fontStyle = fontStyle,
                        fontWeight = fontWeight,
                        fontFamily = fontFamily,
                        letterSpacing = letterSpacing,
                        textDecoration = textDecoration,
                        textAlign = textAlign,
                        lineHeight = lineHeight,
                        overflow = TextOverflow.Visible,
                        softWrap = false,
                        maxLines = 1,
                        minLines = 1,
                        onTextLayout = onTextLayout,
                        style = style
                    )
                }
            }
        }
    }
}

private fun LayoutDirection.reversed() = when (this) {
    LayoutDirection.Ltr -> LayoutDirection.Rtl
    LayoutDirection.Rtl -> LayoutDirection.Ltr
}

@Preview
@Composable
private fun PreviewCounter() {
    var count by remember { mutableIntStateOf(0) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = { count-- }) { Text("-") }
        Counter(count, fontSize = 20.sp)
        Button(onClick = { count++ }) { Text("+") }
    }
}
