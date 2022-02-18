package de.westnordost.streetcomplete.data.meta

import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern

/** Road abbreviations for all languages */
class Abbreviations(config: Map<String, String>, val locale: Locale) {
    private val abbreviations = config.map { (key, value) ->
        var abbreviation = key.lowercase(locale)
        var expansion = value.lowercase(locale)

        if (abbreviation.endsWith("$")) {
            abbreviation = abbreviation.substring(0, abbreviation.length - 1) + "\\.?$"
        } else {
            abbreviation += "\\.?"
        }

        if (abbreviation.startsWith("...")) {
            abbreviation = "(\\w*)" + abbreviation.substring(3)
            expansion = "$1$expansion"
        }

        return@map abbreviation to expansion
    }.toMap()

    /**
     * @param word the word that might be an abbreviation for something
     * @param isFirstWord whether the given word is the first word in the name
     * @param isLastWord whether the given word is the last word in the name
     * @return the expansion of the abbreviation if word is an abbreviation for something,
     * otherwise null
     */
    fun getExpansion(word: String, isFirstWord: Boolean, isLastWord: Boolean): String? {
        for ((pattern, replacement) in abbreviations) {
            val matcher = getMatcher(word, pattern, isFirstWord, isLastWord)
            if (matcher == null || !matcher.matches()) continue
            val result = matcher.replaceFirst(replacement)
            return result.firstLetterToUppercase()
        }
        return null
    }

    private fun getMatcher(
        word: String,
        pattern: String,
        isFirstWord: Boolean,
        isLastWord: Boolean,
    ): Matcher? {
        if (pattern.startsWith("^") && !isFirstWord) return null
        if (pattern.endsWith("$") && !isLastWord) return null

        val patternFlags = Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE
        val p = Pattern.compile(pattern, patternFlags)
        val matcher = p.matcher(word)

        /* abbreviations that are marked to only appear at the end of the name do not
           match with the first word the user is typing. I.e. if the user types "St. ", it will
           not expand to "Street " because it is also the first and only word so far

           UNLESS the word is actually concatenated, i.e. German "Königstr." is expanded to
           "Königstraße" (but "Str. " is not expanded to "Straße") */
        if (pattern.endsWith("$") && isFirstWord) {
            val isConcatenated =
                matcher.matches() && matcher.groupCount() > 0 && !matcher.group(1).isNullOrEmpty()
            if (!isConcatenated) return null
        }

        return matcher
    }

    /** @return whether any word in the given name matches with an abbreviation */
    fun containsAbbreviations(name: String): Boolean {
        val words = name.split("[ -]+".toRegex()).toTypedArray()
        words.forEachIndexed { index, word ->
            for (pattern in abbreviations.keys) {
                val matcher = getMatcher(word, pattern, index == 0, index == words.size - 1)
                if (matcher != null && matcher.matches()) return true
            }
        }
        return false
    }

    private fun String.firstLetterToUppercase() =
        this.substring(0, 1).uppercase(locale) + this.substring(1)
}
