package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorProducer
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.theme.AppTheme
import de.westnordost.streetcomplete.ui.util.toAnnotatedString
import de.westnordost.streetcomplete.util.html.HtmlNode
import de.westnordost.streetcomplete.util.html.tryParseHtml

@Composable
fun HtmlText(
    html: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    inlineContent: Map<String, InlineTextContent> = mapOf(),
    onTextLayout: (TextLayoutResult) -> Unit = {},
) {
    val htmlNodes = remember(html) { tryParseHtml(html) }
    HtmlText(
        html = htmlNodes,
        modifier = modifier,
        style = style,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        inlineContent = inlineContent,
        onTextLayout = onTextLayout,
    )
}

@Composable
fun HtmlText(
    html: List<HtmlNode>,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    inlineContent: Map<String, InlineTextContent> = mapOf(),
    onTextLayout: (TextLayoutResult) -> Unit = {},
) {
    val annotatedString = html.toAnnotatedString()
    Text(
        text = annotatedString,
        modifier = modifier,
        style = style,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        inlineContent = inlineContent,
        onTextLayout = onTextLayout,
    )
}

@PreviewLightDark
@Composable
private fun HtmlTextPreview() {
    AppTheme { Surface {
        HtmlText("""normal
    <b>bold</b>
    <i>italic</i>
    <s>strike</s>
    <u>underline</u>
    <tt>code</tt>
    <sup>superspace</sup>
    <sub>subspace</sub>
    <big>big</big>
    <small>small</small>
    <a href="url">link</a>
    <mark>mark</mark><br>
    <h1>h1</h1>
    <h2>h2</h2>
    <h3>h3</h3>
    <h4>h4</h4>
    <h5>h5</h5>
    <h6>h6</h6>
    <ul>
    <li>The bullet symbol may take any of a variety of shapes such as circular, square, diamond or arrow.</li>
    <li>Typical word processor software offers a wide selection of shapes and colors</li>
    </ul>
    <p>Paragraph</p>
    <blockquote>A block quotation is a quotation in a written document that is set off from the main text as a paragraph, or block of text, and typically distinguished visually using indentation.</blockquote>
    """,
            modifier = Modifier.width(320.dp))
    } }
}
