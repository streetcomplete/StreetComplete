package de.westnordost.streetcomplete.ui.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink

/** Replace placeholders like %1$s etc. with [formatArgs] which may be `AnnotatedString`s */
fun String.formatAnnotated(vararg formatArgs: Any): AnnotatedString {
    val string = this
    val regex = Regex("%([0-9]+)\\$[sd]")
    val matches = regex.findAll(string)
    var lastIndex = 0

    return buildAnnotatedString {
        for (match in matches) {
            append(string.substring(lastIndex, match.range.first))

            val argIndex = match.groupValues[1].toInt() - 1
            when (val arg = formatArgs.getOrNull(argIndex)) {
                is AnnotatedString -> append(arg)
                else -> append(arg?.toString().orEmpty())
            }

            lastIndex = match.range.last + 1
        }

        if (lastIndex < string.length) {
            append(string.substring(lastIndex))
        }
    }
}

/** Annotate any hyperlinks written within this string as clickable links */
fun String.annotateLinks(textLinkStyles: TextLinkStyles?): AnnotatedString {
    val string = this
    val regex = Regex("(?:^|[\\s])(https?://[a-zA-Z0-9-._~!\$&'()*+,;=:/?#\\[\\]@%]+)")
    val matches = regex.findAll(string)
    var lastIndex = 0

    return buildAnnotatedString {
        for (match in matches) {
            val url = match.groupValues[1]
            append(string.substring(lastIndex, match.range.first))
            withLink(LinkAnnotation.Url(url, textLinkStyles)) {
                append(url)
            }
            lastIndex = match.range.last + 1
        }

        append(string.substring(lastIndex))
    }
}
