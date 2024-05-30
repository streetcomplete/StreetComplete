package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import de.westnordost.streetcomplete.ui.util.toAnnotatedString
import de.westnordost.streetcomplete.util.html.HtmlNode
import de.westnordost.streetcomplete.util.html.parseHtml

@Composable
fun HtmlText(
    html: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    softWrap: Boolean = true,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    onClickLink: (String) -> Unit
) {
    HtmlText(
        parseHtml(html),
        modifier = modifier,
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
    style: TextStyle = TextStyle.Default,
    softWrap: Boolean = true,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    onClickLink: (String) -> Unit
) {
    val annotatedString = html.toAnnotatedString()
    ClickableText(
        text = annotatedString,
        modifier = modifier,
        style = style,
        softWrap = softWrap,
        overflow = overflow,
        maxLines = maxLines,
    ) { offset ->
        val link = annotatedString.getUrlAnnotations(offset, offset).firstOrNull()?.item?.url
        if (link != null) { onClickLink(link) }
    }
}

@Preview
@Composable
private fun HtmlTextPreview() {
    HtmlText("""
    normal
    <b>bold</b>
    <i>italic</i>
    <s>strike</s>
    <u>underline</u>
    <tt>code</tt><br>
    <sup>superspace</sup>
    <sub>subspace</sub>
    <big>big</big>
    <small>small</small><br>
    <a href="url">link</a>
    <mark>mark</mark><br>
    <h1>h1</h1>
    <h2>h2</h2>
    <h3>h3</h3>
    <h4>h4</h4>
    <h5>h5</h5>
    <h6>h6</h6>
    """.trimIndent()) {}
}
