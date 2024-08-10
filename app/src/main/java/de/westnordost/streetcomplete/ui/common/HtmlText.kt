package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.ExperimentalTextApi
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
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current,
    softWrap: Boolean = true,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    onClickLink: (String) -> Unit
) {
    val htmlNodes = remember(html) { tryParseHtml(html) }
    HtmlText(
        html = htmlNodes,
        modifier = modifier,
        color = color,
        style = style,
        softWrap = softWrap,
        overflow = overflow,
        maxLines = maxLines,
        onClickLink = onClickLink
    )
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun HtmlText(
    html: List<HtmlNode>,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current,
    softWrap: Boolean = true,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    onClickLink: (String) -> Unit
) {
    val annotatedString = html.toAnnotatedString()
    val styleWithColor = style.copy(color = color.takeOrElse { LocalContentColor.current })
    ClickableText(
        text = annotatedString,
        modifier = modifier,
        style = styleWithColor,
        softWrap = softWrap,
        overflow = overflow,
        maxLines = maxLines,
    ) { offset ->
        val link = annotatedString.getUrlAnnotations(offset, offset).firstOrNull()?.item?.url
        if (link != null) { onClickLink(link) }
    }
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
            modifier = Modifier.width(320.dp)) {}
    } }
}
