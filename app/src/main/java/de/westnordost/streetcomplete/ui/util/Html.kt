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
    val builder = AnnotatedString.Builder()
    builder.append(this)
    return builder.toAnnotatedString()
}

@SuppressLint("ComposableNaming")
@Composable
private fun AnnotatedString.Builder.append(nodes: List<HtmlNode>) {
    nodes.forEachIndexed { i, node ->
        val nextNode = nodes.getOrNull(i + 1)
        // ignore blank elements before block elements
        if (nextNode?.isBlockElement() != true || !node.isBlankText()) {
            append(node)
        }
    }
}

@SuppressLint("ComposableNaming")
@Composable
private fun AnnotatedString.Builder.append(node: HtmlNode) {
    if (node is HtmlElementNode) append(node)
    else if (node is HtmlTextNode) append(node.text)
}

@SuppressLint("ComposableNaming")
@OptIn(ExperimentalTextApi::class)
@Composable
private fun AnnotatedString.Builder.append(element: HtmlElementNode) {
    if (element.tag == "br") {
        append('\n')
        return
    }

    val paragraph = when (element.tag) {
        "h1", "h2", "h3", "h4", "h5", "h6", "p", "div" -> {
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
    if (paragraph != null) {
        // Compose doesn't allow nesting paragraphs, so we pop everything before adding a new one
        tryPopAll()
        pushStyle(paragraph)
    }

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
        "a" -> {
            val linkColor = MaterialTheme.colors.secondary
            SpanStyle(textDecoration = TextDecoration.Underline, color = linkColor)
        }
        else -> null
    }
    if (span != null) pushStyle(span)
    if (element.tag == "a") pushUrlAnnotation(UrlAnnotation(element.attributes["href"].orEmpty()))

    if (paragraph != null) append('\n')
    if (element.tag == "li") append(bullet)

    append(element.nodes)

    if (element.tag == "a") tryPop()
    if (span != null) tryPop()
    if (paragraph != null) tryPop()
}

private fun AnnotatedString.Builder.tryPopAll() {
    try { pop(0) } catch (_: Exception) {}
}

private fun AnnotatedString.Builder.tryPop() {
    try { pop() } catch (_: Exception) {}
}

private fun HtmlNode.isBlockElement(): Boolean =
    this is HtmlElementNode && this.tag in blockElements

private fun HtmlNode.isBlankText(): Boolean =
    this is HtmlTextNode && this.text.isBlank()

private const val indent = 24f
private const val bullet = "●  "

private val blockElements = setOf(
    "h1", "h2", "h3", "h4", "h5", "h6", "p", "div", "ul", "blockquote", "li"
)
