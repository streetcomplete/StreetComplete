package de.westnordost.streetcomplete.util.ktx

/** Return the first and last element of this list. If it contains only one element, just that one */
fun <E> List<E>.firstAndLast() = if (size == 1) listOf(first()) else listOf(first(), last())

/** Return all except the first and last element of this list. If it contains less than two elements,
 *  it returns an empty list */
fun <E> List<E>.allExceptFirstAndLast() = if (size > 2) subList(1, size - 1) else listOf()

/** Returns whether the collection contains any of the [elements] */
fun <E> Collection<E>.containsAny(elements: Collection<E>) = elements.any { contains(it) }

/** Returns `true` if at least one element matches the given [predicate]. */
inline fun <T> Collection<T>.anyIndexed(predicate: (index: Int, T) -> Boolean): Boolean {
    this.forEachIndexed { index, element -> if (predicate(index, element)) return true }
    return false
}

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

/** Return a sequence that iterates through the given list of points in pairs */
fun <T> Iterable<T>.asSequenceOfPairs(): Sequence<Pair<T, T>> = sequence {
    val it = iterator()
    if (!it.hasNext()) return@sequence
    var item1 = it.next()
    while (it.hasNext()) {
        val item2 = it.next()
        yield(item1 to item2)
        item1 = item2
    }
}

/** returns the index of the first element yielding the largest value of the given function or -1
 *  if there are no elements. Analogous to the maxBy extension function. */
inline fun <T, R : Comparable<R>> Iterable<T>.indexOfMaxBy(selector: (T) -> R): Int {
    val iterator = iterator()
    if (!iterator.hasNext()) return -1
    var indexOfMaxElem = 0
    var i = 0
    var maxValue = selector(iterator.next())
    while (iterator.hasNext()) {
        ++i
        val v = selector(iterator.next())
        if (maxValue < v) {
            indexOfMaxElem = i
            maxValue = v
        }
    }
    return indexOfMaxElem
}

inline fun <T> Iterable<T>.sumByFloat(selector: (T) -> Float): Float {
    var sum = 0f
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

fun <T> Collection<T>.containsExactlyInAnyOrder(other: Collection<T>): Boolean =
    other.size == size && containsAll(other)

/** Returns a new read-only array only of those given [elements] that are not null. */
inline fun <reified T> arrayOfNotNull(vararg elements: T?): Array<T> =
    elements.filterNotNull().toTypedArray()
