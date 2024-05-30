package de.westnordost.streetcomplete.ui.util

import android.annotation.SuppressLint
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.UrlAnnotation
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.sp
import de.westnordost.streetcomplete.ui.ktx.pxToSp
import de.westnordost.streetcomplete.util.html.HtmlElementNode
import de.westnordost.streetcomplete.util.html.HtmlNode
import de.westnordost.streetcomplete.util.html.HtmlTextNode

@Composable
fun List<HtmlNode>.toAnnotatedString(): AnnotatedString {
    val linkColor = MaterialTheme.colors.secondary
    val builder = AnnotatedString.Builder()
    for (node in this) builder.append(node, linkColor, null)
    return builder.toAnnotatedString()
}

@SuppressLint("ComposableNaming")
@Composable
private fun AnnotatedString.Builder.append(
    node: HtmlNode,
    linkColor: Color,
    currentParagraphIndex: Int?
) {
    if (node is HtmlElementNode) append(node, linkColor, currentParagraphIndex)
    else if (node is HtmlTextNode) append(node.text)
}

@SuppressLint("ComposableNaming")
@OptIn(ExperimentalTextApi::class)
@Composable
private fun AnnotatedString.Builder.append(
    element: HtmlElementNode,
    linkColor: Color,
    currentParagraphIndex: Int?
) {
    if (element.tag == "br") {
        append('\n')
        return
    }

    val paragraph = when (element.tag) {
        "h1", "h2", "h3", "h4", "h5", "h6", "p" -> {
            ParagraphStyle()
        }
        "blockquote" -> {
            ParagraphStyle(textIndent = TextIndent(indent.sp, indent.sp))
        }
        "li" -> {
            val textStyle = LocalTextStyle.current
            val textMeasurer = rememberTextMeasurer()
            val bulletWidth = remember(textStyle, textMeasurer) {
                textMeasurer.measure(text = bullet, style = textStyle).size.width
            }
            val bulletWidthSp = bulletWidth.pxToSp()
            ParagraphStyle(
                textIndent = TextIndent(
                    firstLine = (indent - bulletWidthSp.value).sp,
                    restLine = indent.sp
                )
            )
        }
        else -> null
    }
    val paragraphIndex = if (paragraph != null) {
        // Compose doesn't allow nesting paragraphs, so we need to pop the old one before
        if (currentParagraphIndex != null) pop(currentParagraphIndex)
        pushStyle(paragraph)
    } else null
    if (element.tag == "li") append(bullet)

    val span = when (element.tag) {
        // Spans
        "h1", "h2", "h3", "h4", "h5", "h6" -> {
            val sizes = arrayOf(2.0f, 1.5f, 1.17f, 1.0f, 0.83f, 0.67f)
            val size = sizes[element.tag.last().digitToInt() - 1]
            SpanStyle(fontWeight = FontWeight.Bold, fontSize = TextUnit(size, TextUnitType.Em))
        }
        "b", "strong" ->
            SpanStyle(fontWeight = FontWeight.Bold)
        "i", "em", "dfn", "cite", "var" ->
            SpanStyle(fontStyle = FontStyle.Italic)
        "s", "strike", "del" ->
            SpanStyle(textDecoration = TextDecoration.LineThrough)
        "u", "ins" ->
            SpanStyle(textDecoration = TextDecoration.Underline)
        "tt", "code", "kbd", "samp" ->
            SpanStyle(fontFamily = FontFamily.Monospace, background = Color(0x33bbbbbb))
        "sup" ->
            SpanStyle(baselineShift = BaselineShift.Superscript, fontSize = TextUnit(0.8f, TextUnitType.Em))
        "sub" ->
            SpanStyle(baselineShift = BaselineShift.Subscript, fontSize = TextUnit(0.8f, TextUnitType.Em))
        "big" ->
            SpanStyle(fontSize = TextUnit(1.25f, TextUnitType.Em))
        "small" ->
            SpanStyle(fontSize = TextUnit(0.8f, TextUnitType.Em))
        "mark" ->
            SpanStyle(background = Color.Yellow)
        "span" ->
            SpanStyle()
        "a" ->
            SpanStyle(textDecoration = TextDecoration.Underline, color = linkColor)
        else -> null
    }
    if (span != null) pushStyle(span)
    if (element.tag == "a") pushUrlAnnotation(UrlAnnotation(element.attributes["href"].orEmpty()))

    for (node in element.nodes) {
        append(node, linkColor, paragraphIndex)
    }

    if (element.tag == "a") pop()
    if (span != null) pop()
    if (paragraph != null) pop()
}

private const val indent = 24f
private const val bullet = "●  "
