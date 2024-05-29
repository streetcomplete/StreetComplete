package de.westnordost.streetcomplete.ui.util

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import de.westnordost.streetcomplete.util.html.HtmlElement
import de.westnordost.streetcomplete.util.html.HtmlNode
import de.westnordost.streetcomplete.util.html.HtmlText
import de.westnordost.streetcomplete.util.html.parseHtmlMarkup

@Preview
@Composable
fun Html() {
    Text(parseHtmlMarkup("""
    <b>bold</b><br>
    <i>italic</i><br>
    <s>strike</s><br>
    <u>underline</u><br>
    <tt>monospace</tt><br>
    <sup>superspace</sup><br>
    <sub>subspace</sub><br>
    <big>big</big><br>
    <small>small</small><br>
    <a href="url">link</a><br>
    <mark>mark</mark><br>
    """.trimIndent()).toAnnotatedString())
}

@Composable
fun List<HtmlNode>.toAnnotatedString(): AnnotatedString {
    val linkColor = MaterialTheme.colors.secondary
    val builder = AnnotatedString.Builder()
    for (node in this) builder.append(node, linkColor)
    return builder.toAnnotatedString()
}

private fun AnnotatedString.Builder.append(node: HtmlNode, linkColor: Color) {
    if (node is HtmlElement) append(node, linkColor)
    else if (node is HtmlText) append(node.text.replace('\n', ' '))
}

@OptIn(ExperimentalTextApi::class)
private fun AnnotatedString.Builder.append(element: HtmlElement, linkColor: Color) {
    if (element.tag == "br") {
        append('\n')
        return
    }

    val span = when (element.tag) {
        // Spans
        "b", "strong" ->
            SpanStyle(fontWeight = FontWeight.Bold)
        "i", "em", "dfn", "cite", "var" ->
            SpanStyle(fontStyle = FontStyle.Italic)
        "s", "strike", "del" ->
            SpanStyle(textDecoration = TextDecoration.LineThrough)
        "u", "ins" ->
            SpanStyle(textDecoration = TextDecoration.Underline)
        "tt", "code", "kbd", "samp" ->
            SpanStyle(fontFamily = FontFamily.Monospace)
        "sup" ->
            SpanStyle(baselineShift = BaselineShift.Superscript)
        "sub" ->
            SpanStyle(baselineShift = BaselineShift.Subscript)
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
