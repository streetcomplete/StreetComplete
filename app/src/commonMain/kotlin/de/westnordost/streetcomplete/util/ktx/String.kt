package de.westnordost.streetcomplete.util.ktx

/** Make sure that the string is not longer than [length], adding "…" for truncation */
fun String.truncate(length: Int): String =
    if (this.length > length) substring(0, length - 1) + "…" else this

/** Return whether this string contains all given [words] */
fun String.containsAll(words: Collection<String>) = words.all { this.contains(it) }

/** Returns the indices of all occurrences of [substring]. */
fun String.indicesOf(substring: String): Sequence<Int> = sequence {
    var previousIndex = -1
    while (true) {
        previousIndex = indexOf(substring, previousIndex + 1)
        if (previousIndex == -1) {
            break
        }
        yield(previousIndex)
    }
}
