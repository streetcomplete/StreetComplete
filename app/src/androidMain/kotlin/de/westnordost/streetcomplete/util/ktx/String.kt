package de.westnordost.streetcomplete.util.ktx

fun String.truncate(length: Int): String =
    if (this.length > length) substring(0, length - 1) + "…" else this

fun String.containsAll(words: List<String>) = words.all { this.contains(it) }

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
