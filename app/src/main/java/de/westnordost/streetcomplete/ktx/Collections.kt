package de.westnordost.streetcomplete.ktx

/** Return the first and last element of this list. If it contains only one element, just that one */
fun <E> List<E>.firstAndLast() = if (size == 1) listOf(first()) else listOf(first(), last())

/** Returns whether the collection contains any of the [elements] */
fun <E> Collection<E>.containsAny(elements: Collection<E>) = elements.any { contains(it) }

/**
 * Starting at [index] (exclusive), iterating the list in reverse, returns the first element that
 * matches the given [predicate], or `null` if no such element was found.
 */
inline fun <T> List<T>.findPrevious(index: Int, predicate: (T) -> Boolean): T? {
    val iterator = this.listIterator(index)
    while (iterator.hasPrevious()) {
        val element = iterator.previous()
        if (predicate(element)) return element
    }
    return null
}

/**
 * Starting at [index] (inclusive), iterating the list, returns the first element that
 * matches the given [predicate], or `null` if no such element was found.
 */
inline fun <T> List<T>.findNext(index: Int, predicate: (T) -> Boolean): T? {
    val iterator = this.listIterator(index)
    while (iterator.hasNext()) {
        val element = iterator.next()
        if (predicate(element)) return element
    }
    return null
}
