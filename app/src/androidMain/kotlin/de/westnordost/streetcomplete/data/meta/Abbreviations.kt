package de.westnordost.streetcomplete.data.meta

import de.westnordost.streetcomplete.util.ktx.anyIndexed
import java.util.Locale

/** Road abbreviations for all languages */
class Abbreviations(config: Map<String, String>, val locale: Locale) {
    private val abbreviations = config.map { (abbreviation, expansion) ->
        var pattern = abbreviation.lowercase(locale)
        var replacement = expansion.lowercase(locale)

        if (pattern.endsWith("$")) {
            pattern = pattern.dropLast(1) + "\\.?$"
        } else {
            pattern += "\\.?"
        }

        if (pattern.startsWith("...")) {
            pattern = "(\\w*)" + pattern.drop(3)
            replacement = "$1$replacement"
        }

        val regex = pattern.toRegex(RegexOption.IGNORE_CASE)

        return@map regex to replacement
    }.toMap()

    /**
     * @param word the word that might be an abbreviation for something
     * @param isFirstWord whether the given word is the first word in the name
     * @param isLastWord whether the given word is the last word in the name
     * @return the expansion of the abbreviation if word is an abbreviation for something,
     * otherwise null
     */
    fun getExpansion(word: String, isFirstWord: Boolean, isLastWord: Boolean): String? {
        for ((regex, replacement) in abbreviations) {
            if (!regex.matches(word, isFirstWord, isLastWord)) continue
            val result = regex.replaceFirst(word, replacement)
            return if (word.first().isTitleCase()) result.titlecase(locale) else result
        }
        return null
    }

    /** @return whether any word in the given name matches with an abbreviation */
    fun containsAbbreviations(name: String): Boolean {
        val words = name.split("[ -]+".toRegex())
        return words.anyIndexed { index, word ->
            val isFirstWord = index == 0
            val isLastWord = index == words.size - 1
            abbreviations.any { (regex, _) -> regex.matches(word, isFirstWord, isLastWord) }
        }
    }

    private fun Regex.matches(
        word: String,
        isFirstWord: Boolean,
        isLastWord: Boolean,
    ): Boolean {
        if (pattern.startsWith("^") && !isFirstWord) return false
        if (pattern.endsWith("$") && !isLastWord) return false

        /* abbreviations that are marked to only appear at the end of the name do not
           match with the first word the user is typing. I.e. if the user types "St. ", it will
           not expand to "Street " because it is also the first and only word so far

           UNLESS the word is actually concatenated, i.e. German "Königstr." is expanded to
           "Königstraße" (but "Str. " is not expanded to "Straße") */
        if (pattern.endsWith("$") && isFirstWord) {
            val groupMatch = this.find(word)?.groupValues?.getOrNull(1)
            return !groupMatch.isNullOrEmpty()
        }

        return this.matches(word)
    }
}

private fun String.titlecase(locale: Locale) = get(0).titlecase(locale) + substring(1)
