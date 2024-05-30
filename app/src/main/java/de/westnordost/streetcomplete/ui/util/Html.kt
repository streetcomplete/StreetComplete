package de.westnordost.streetcomplete.ui.util

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.UrlAnnotation
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import de.westnordost.streetcomplete.util.html.HtmlElementNode
import de.westnordost.streetcomplete.util.html.HtmlNode
import de.westnordost.streetcomplete.util.html.HtmlTextNode

@Composable
@ReadOnlyComposable
fun List<HtmlNode>.toAnnotatedString(): AnnotatedString {
    val linkColor = MaterialTheme.colors.secondary
    val builder = AnnotatedString.Builder()
    for (node in this) builder.append(node, linkColor)
    return builder.toAnnotatedString()
}

private fun AnnotatedString.Builder.append(node: HtmlNode, linkColor: Color) {
    if (node is HtmlElementNode) append(node, linkColor)
    // linebreaks in HTML are treated as spaces, use <br> for linebreaks
    else if (node is HtmlTextNode) append(node.text.replace('\n', ' '))
}

@OptIn(ExperimentalTextApi::class)
private fun AnnotatedString.Builder.append(element: HtmlElementNode, linkColor: Color) {
    if (element.tag == "br") {
        append('\n')
        return
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
        "a" ->
            SpanStyle(textDecoration = TextDecoration.Underline, color = linkColor)
        else -> null
    }
    if (span != null) pushStyle(span)
    if (element.tag == "a") pushUrlAnnotation(UrlAnnotation(element.attributes["href"].orEmpty()))

    for (node in element.nodes) append(node, linkColor)

    if (element.tag == "a") pop()
    if (span != null) pop()
}
