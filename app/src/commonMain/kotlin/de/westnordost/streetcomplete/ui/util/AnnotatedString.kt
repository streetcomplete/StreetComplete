package de.westnordost.streetcomplete.ui.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString

/** Replace placeholders like %1$s etc. with [formatArgs] which may be `AnnotatedString`s */
fun String.formatAnnotated(vararg formatArgs: Any): AnnotatedString =
    buildAnnotatedString {
        val regex = Regex("%([0-9]+)\\$[sd]")
        val matches = regex.findAll(this@formatAnnotated).toList()

        var lastIndex = 0

        for (match in matches) {
            append(this@formatAnnotated.substring(lastIndex, match.range.first))

            val argIndex = match.groupValues[1].toInt() - 1
            when (val arg = formatArgs.getOrNull(argIndex)) {
                is AnnotatedString -> append(arg)
                else -> append(arg?.toString().orEmpty())
            }

            lastIndex = match.range.last + 1
        }

        if (lastIndex < this@formatAnnotated.length) {
            append(this@formatAnnotated.substring(lastIndex))
        }
    }
